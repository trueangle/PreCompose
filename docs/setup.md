# Setup

## Add Dependency
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose/badge.svg)](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose)

Add the dependency **in your common module's commonMain sourceSet**
```
// Please do remember to add compose.foundation and compose.animation
api(compose.foundation)
api(compose.animation)
//...
api("moe.tlaster:precompose:$precompose_version")

// api("moe.tlaster:precompose-molecule:$precompose_version") // For Molecule intergration 

// api("moe.tlaster:precompose-viewmodel:$precompose_version") // For ViewModel intergration

// api("moe.tlaster:precompose-koin:$precompose_version") // For Koin intergration
```

## Wrap the `App()`

Wrap your App with `PreComposApp` like this:
```Kotlin
@Composable
fun App() {
    PreComposeApp {
        // your app's content goes here
    }
}
```

## Done!
That's it! Enjoying the PreCompose! Now you can write all your business logic and ui code in `commonMain`
