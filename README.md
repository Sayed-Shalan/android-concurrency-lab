# android-concurrency-lab

Runnable Kotlin samples, checked by tests, for the concurrency and networking behavior that is easy to get wrong.

This project is JVM-only. Lifecycle cancellation of a `viewModelScope` is covered by the view-model test in `android-architecture-blueprint`, where a `ViewModelStore` is cleared while a repository is suspended. Repeating that Android test here would not show a different mechanism.

## What the tests exercise

- A thread keeps running until `join`. A `coroutineScope` does not return until its child finishes.
- `withContext(Dispatchers.Default)` leaves the test dispatcher thread.
- Cancelling a parent runs the child's `finally` block.
- A failing child of `coroutineScope` cancels its sibling. The same failure under `supervisorScope` does not.
- `CancellationException` is rethrown. Other failures can become `null`.
- Retrofit with OkHttp calls `mockwebserver3.MockWebServer` and decodes a JSON body with Moshi. OkHttp 5.5.1 keeps the old `okhttp3.mockwebserver` types as a deprecated bridge, so the test uses the `mockwebserver3` artifact. No device and no real network.

## Versions

Checked against Maven metadata on 23 October 2026.

- Gradle 9.6.0
- Kotlin 2.4.21
- Coroutines 1.11.0
- OkHttp 5.5.1 and MockWebServer 5.5.1 (`mockwebserver3`)
- Retrofit 3.0.0
- Moshi 1.15.2

## Run

```bash
./gradlew test
```

## License

[MIT](LICENSE)
