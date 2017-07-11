import com.google.gson.Gson
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

val gson = Gson()

object ActionsTest: Spek({
    describe("Working test") {
        on(" running") {
            it("Should pass") {
                true
            }
        }
    }
})