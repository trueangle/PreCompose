package moe.tlaster.precompose.navigation

import androidx.compose.runtime.Composable

class RouteBuilder(
    private val initialRoute: String,
) {
    private val route = mutableListOf<Route>()

    fun scene(
        route: String,
        content: @Composable (stack : RouteStackManager.Stack) -> Unit,
    ) {
        this.route += Route(
            route = route,
            content = content,
        )
    }

    fun dialog(
        route: String,
        content: @Composable () -> Unit,
    ) {

    }

    fun build() = RouteGraph(initialRoute, route.toList())
}