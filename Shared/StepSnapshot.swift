import Foundation

/// The step total for one calendar day.
struct DailySteps: Codable, Hashable, Identifiable, Sendable {
    /// Start of the day, in the user's current calendar.
    var date: Date
    var steps: Int

    var id: Date { date }
}

/// A point-in-time copy of recent daily step totals, cached in the App Group
/// so the widget has something to show while HealthKit is locked.
struct StepSnapshot: Codable, Hashable, Sendable {
    /// When the totals were read from HealthKit.
    var fetchedAt: Date
    /// Daily totals, oldest first, ending on the day of `fetchedAt`.
    var days: [DailySteps]

    static let empty = StepSnapshot(fetchedAt: .distantPast, days: [])

    /// Steps on the day containing `date`. Days the snapshot doesn't cover
    /// (such as a new day that started after it was taken) count as 0.
    func steps(on date: Date, calendar: Calendar = .current) -> Int {
        days.first { calendar.isDate($0.date, inSameDayAs: date) }?.steps ?? 0
    }

    /// The `count` days ending on the day containing `date`, oldest first.
    func week(endingOn date: Date, count: Int = 7, calendar: Calendar = .current) -> [DailySteps] {
        let lastDay = calendar.startOfDay(for: date)
        return (0..<count).reversed().compactMap { offset in
            guard let day = calendar.date(byAdding: .day, value: -offset, to: lastDay) else { return nil }
            return DailySteps(date: day, steps: steps(on: day, calendar: calendar))
        }
    }
}

extension [DailySteps] {
    var totalSteps: Int { reduce(0) { $0 + $1.steps } }

    var averageSteps: Int { isEmpty ? 0 : totalSteps / count }

    func daysMeeting(goal: Int) -> Int { filter { $0.steps >= goal }.count }
}

extension StepSnapshot {
    /// Realistic sample data for previews, placeholders and the widget gallery.
    static func sample(now: Date = .now, calendar: Calendar = .current) -> StepSnapshot {
        let values = [6_240, 8_915, 11_302, 7_480, 10_870, 12_456, 7_532]
        let today = calendar.startOfDay(for: now)
        let days = values.enumerated().compactMap { index, steps -> DailySteps? in
            guard let day = calendar.date(byAdding: .day, value: index - (values.count - 1), to: today) else { return nil }
            return DailySteps(date: day, steps: steps)
        }
        return StepSnapshot(fetchedAt: now, days: days)
    }
}
