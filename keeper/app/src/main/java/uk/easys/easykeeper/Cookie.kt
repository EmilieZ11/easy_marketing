package uk.easys.easykeeper

class Cookie(val name: String, val value: String) {
    companion object {
        fun parse(cookies: String): List<Cookie> {
            var list = ArrayList<Cookie>()
            var split = cookies.split("; ")
            for (s in split) {
                var splOne = s.split("=")
                list.add(Cookie(splOne[0], if (splOne.size > 1) splOne[1] else ""))
            }
            return list
        }

        fun findLogin(list: List<Cookie>): Cookie? {
            var c: Cookie? = null
            for (i in list) if (i.name.length >= 20) if (i.name.substring(0, 20) == "wordpress_logged_in_") c = i
            return c
        }
    }
}
