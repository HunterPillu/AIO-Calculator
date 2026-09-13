package app.prinkal.calculator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform