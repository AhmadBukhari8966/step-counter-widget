import WidgetKit

/// Everything a widget needs to draw one moment of its timeline.
struct StepsEntry: TimelineEntry {
    enum Status: Sendable {
        case ready
        case needsHealthAccess
        case healthUnavailable
    }

    var date: Date
    var snapshot: StepSnapshot?
    var goal: Int
    var status: Status = .ready
    var theme: StepTheme = .classic

    /// Steps for the entry's day. A snapshot from yesterday reads as 0 after midnight.
    var steps: Int { snapshot?.steps(on: date) ?? 0 }
    var progress: Double { Double(steps) / Double(max(goal, 1)) }
    var goalReached: Bool { steps >= goal }
    var remaining: Int { max(goal - steps, 0) }
    var week: [DailySteps] { (snapshot ?? .empty).week(endingOn: date) }
}

extension StepsEntry {
    static func sample(theme: StepTheme = .classic, date: Date = .now) -> StepsEntry {
        StepsEntry(date: date, snapshot: .sample(now: date), goal: SharedStore.defaultGoal, theme: theme)
    }
}

enum StepsTimeline {
    /// How often to ask for a new timeline. WidgetKit may stretch this to save battery.
    static let refreshInterval: TimeInterval = 15 * 60

    /// Reads fresh totals from HealthKit when it can. HealthKit can't be read while the
    /// device is locked, so otherwise it falls back to the last totals the app or widget saved.
    static func entry(at date: Date = .now, theme: StepTheme) async -> StepsEntry {
        let goal = SharedStore.goal
        guard HealthSteps.isAvailable else {
            return StepsEntry(date: date, snapshot: nil, goal: goal, status: .healthUnavailable, theme: theme)
        }

        do {
            let snapshot = try await HealthSteps.fetchSnapshot(now: date)
            SharedStore.save(snapshot)
            return StepsEntry(date: date, snapshot: snapshot, goal: goal, theme: theme)
        } catch {
            let cached = SharedStore.loadSnapshot()
            if cached == nil, await HealthSteps.shouldRequestAuthorization() {
                return StepsEntry(date: date, snapshot: nil, goal: goal, status: .needsHealthAccess, theme: theme)
            }
            return StepsEntry(date: date, snapshot: cached, goal: goal, theme: theme)
        }
    }

    static func timeline(theme: StepTheme) async -> Timeline<StepsEntry> {
        let now = Date.now
        let current = await entry(at: now, theme: theme)
        var entries = [current]

        // The count starts over at midnight. Schedule that change up front in case
        // WidgetKit doesn't give the widget a refresh right then.
        let calendar = Calendar.current
        if let midnight = calendar.date(byAdding: .day, value: 1, to: calendar.startOfDay(for: now)) {
            var newDay = current
            newDay.date = midnight
            entries.append(newDay)
        }

        return Timeline(entries: entries, policy: .after(now.addingTimeInterval(refreshInterval)))
    }
}

/// Timeline provider for the Home Screen widget, which has a theme option.
struct StepsHomeProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> StepsEntry {
        .sample()
    }

    func snapshot(for configuration: StepsWidgetConfiguration, in context: Context) async -> StepsEntry {
        let theme = configuration.theme.stepTheme
        // The widget gallery shows sample data until the app has saved real steps.
        if context.isPreview, SharedStore.loadSnapshot() == nil {
            return .sample(theme: theme)
        }
        return await StepsTimeline.entry(theme: theme)
    }

    func timeline(for configuration: StepsWidgetConfiguration, in context: Context) async -> Timeline<StepsEntry> {
        await StepsTimeline.timeline(theme: configuration.theme.stepTheme)
    }
}

/// Timeline provider for the Lock Screen widgets, which have no options.
struct StepsAccessoryProvider: TimelineProvider {
    func placeholder(in context: Context) -> StepsEntry {
        .sample()
    }

    func getSnapshot(in context: Context, completion: @escaping @Sendable (StepsEntry) -> Void) {
        if context.isPreview, SharedStore.loadSnapshot() == nil {
            completion(.sample())
            return
        }
        Task {
            completion(await StepsTimeline.entry(theme: .classic))
        }
    }

    func getTimeline(in context: Context, completion: @escaping @Sendable (Timeline<StepsEntry>) -> Void) {
        Task {
            completion(await StepsTimeline.timeline(theme: .classic))
        }
    }
}
