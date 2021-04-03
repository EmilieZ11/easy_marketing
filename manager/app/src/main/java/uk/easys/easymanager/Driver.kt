package uk.easys.easymanager

import android.util.JsonReader
import android.util.JsonToken
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class Driver(val id: Int, val user: String, val name: String, var ords: ArrayList<Int>) {
    companion object {
        @Throws(IOException::class)
        fun readDrivers(input: InputStream): ArrayList<Driver> {
            var reader = JsonReader(InputStreamReader(input, "UTF-8"))
            try {
                return readDriversArray(reader)
            } finally {
                reader.close()
            }
        }

        @Throws(IOException::class)
        private fun readDriversArray(reader: JsonReader): ArrayList<Driver> {
            var drivers = ArrayList<Driver>()
            reader.beginArray()
            while (reader.hasNext()) drivers.add(readDriver(reader))
            reader.endArray()
            return drivers
        }

        @Throws(IOException::class)
        private fun readDriver(reader: JsonReader): Driver {
            var id = -1
            var user = ""
            var name = ""
            var ords: ArrayList<Int> = ArrayList()

            reader.beginObject()
            while (reader.hasNext()) {
                var param = reader.nextName()
                when(param) {
                    "id" -> id = reader.nextInt()
                    "user" -> user = reader.nextString()
                    "name" -> name = reader.nextString()
                    "ords" -> if (reader.peek() != JsonToken.NULL) ords = readIntArray(reader)
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            return Driver(id, user, name, ords);
        }

        @Throws(IOException::class)
        private fun readIntArray(reader: JsonReader): ArrayList<Int> {
            var integers = ArrayList<Int>()
            reader.beginArray()
            while (reader.hasNext()) integers.add(reader.nextInt())
            reader.endArray()
            return integers
        }
    }
}