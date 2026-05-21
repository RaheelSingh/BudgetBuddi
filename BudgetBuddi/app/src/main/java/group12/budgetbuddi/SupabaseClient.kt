package group12.budgetbuddi

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://raygwqqsmrwtlaviqvei.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJheWd3cXFzbXJ3dGxhdmlxdmVpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkzNjQxNDcsImV4cCI6MjA5NDk0MDE0N30.xP_eLsjYqhGRO0Jm-Ko0BagdgYV8w_yW4-C_ezNfvKw"
    ) {
        install(Postgrest)
        install(Storage)
    }
}