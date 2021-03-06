package io.eberlein.btinformer.objects

import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationCompat
import io.paperdb.Book
import io.paperdb.Paper
import kotlin.collections.ArrayList

enum class FilterType {
    NAME, MAC, UUID
}

class Filter(var name: String, var data: String, var type: String): DBObject() {
    fun applies(ss: Device): Boolean {
        when (FilterType.valueOf(type)) {
            FilterType.MAC -> return ss.address == data
            FilterType.NAME -> return ss.name == data
            FilterType.UUID -> {
                for (u: ParcelUuid in ss.uuids) {
                    if (u.uuid.toString() == data) return true
                }
            }
        }
        return false
    }

    fun apply(lst: ArrayList<Device>): ArrayList<Device> {
        val r = ArrayList<Device>()
        for (ss: Device in lst) {
            if (applies(ss)) r.add(ss)
        }
        return r
    }

    fun notify(nb: NotificationCompat.Builder) {
        nb.setContentTitle("BLE Device found")
        nb.setContentText("Device")
    }

    companion object {
        const val bookName: String = "filter"

        private fun book(): Book {
            return Paper.book(bookName)
        }

        fun all(): ArrayList<Filter> {
            val r = ArrayList<Filter>()
            for(e in book().allKeys) {
                Log.d("Filter.all", e)
                r.add(book().read(e))
            }
            return r
        }

        fun getOrCreate(name: String): Filter {
            var r = book().read<Filter>(name)
            if(r == null) r = Filter("", "", "")
            r.save()
            return r
        }

        fun fromName(name: String): Filter {
            return Filter(name, name, FilterType.NAME.name)
        }

        fun fromMAC(mac: String): Filter {
            return Filter(mac, mac, FilterType.MAC.name)
        }

        fun fromUUID(uuid: String): Filter {
            return Filter(uuid, uuid, FilterType.UUID.name)
        }
    }

    override fun save(){
        book().write(id, this)
    }
}