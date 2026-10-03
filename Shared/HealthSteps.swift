import Foundation
import HealthKit

/// Reads step counts from Apple Health. Used by both the app and the widget extension.
///
/// Only the app can show the permission sheet. Once the user allows access there,
/// the widget extension can read steps too — but only while the device is unlocked,
/// because HealthKit data is encrypted while the iPhone is locked.
enum HealthSteps {
    /// One long-lived store per process, as Apple recommends.
    static let store = HKHealthStore()

    static var isAvailable: Bool { HKHealthStore.isHealthDataAvailable() }

    static var stepType: HKQuantityType { HKQuantityType(.stepCount) }

    /// `true` until the user has been shown the Health permission sheet.
    ///
    /// HealthKit never reveals whether *read* access was granted, only whether
    /// we've asked. If access was denied, queries simply return no steps.
    static func shouldRequestAuthorization() async -> Bool {
        guard isAvailable else { return false }
        let status = try? await store.statusForAuthorizationRequest(toShare: [], read: [stepType])
        return status == .shouldRequest
    }

    static func requestAuthorization() async throws {
        try await store.requestAuthorization(toShare: [], read: [stepType])
    }

    /// Daily step totals for the last `dayCount` days, including today.
    ///
    /// A statistics query merges iPhone and Apple Watch samples without double
    /// counting, so the totals match what the Health app shows.
    static func fetchSnapshot(dayCount: Int = 7, now: Date = .now) async throws -> StepSnapshot {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: now)
        guard let firstDay = calendar.date(byAdding: .day, value: -(dayCount - 1), to: today) else {
            return StepSnapshot(fetchedAt: now, days: [])
        }

        let query = HKStatisticsCollectionQueryDescriptor(
            predicate: .quantitySample(
                type: stepType,
                predicate: HKQuery.predicateForSamples(withStart: firstDay, end: nil, options: .strictStartDate)
            ),
            options: .cumulativeSum,
            anchorDate: today,
            intervalComponents: DateComponents(day: 1)
        )
        let collection = try await query.result(for: store)

        let days = (0..<dayCount).compactMap { offset -> DailySteps? in
            guard let day = calendar.date(byAdding: .day, value: offset, to: firstDay) else { return nil }
            let sum = collection.statistics(for: day)?.sumQuantity()?.doubleValue(for: .count()) ?? 0
            return DailySteps(date: day, steps: Int(sum.rounded()))
        }
        return StepSnapshot(fetchedAt: now, days: days)
    }
}
