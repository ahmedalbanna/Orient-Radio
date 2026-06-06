package com.example.data.repository

import com.example.data.local.FavoriteStationEntity
import com.example.data.local.RadioDao
import com.example.data.model.RadioStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RadioRepository(private val radioDao: RadioDao) {

    // A curated selection of high-quality, extremely stable Arabic-centric streams.
    private val staticStations = listOf(
        RadioStation(
            id = "quran_cairo",
            name = "إذاعة القرآن الكريم (القاهرة)",
            streamUrl = "https://stream.zeno.fm/f36v8pq7f68uv",
            imageUrl = "https://images.unsplash.com/photo-1609599006341-46314f739636?w=400&q=80",
            category = "قرآن كريم",
            country = "مصر"
        ),
        RadioStation(
            id = "quran_ksa",
            name = "إذاعة القرآن الكريم (السعودية)",
            streamUrl = "https://live.mp3quran.net:9702/",
            imageUrl = "https://images.unsplash.com/photo-1597935258735-e254c18395a2?w=400&q=80",
            category = "قرآن كريم",
            country = "السعودية"
        ),
        RadioStation(
            id = "abdelbaset",
            name = "القرآن الكريم بصوت عبد الباسط",
            streamUrl = "https://live.mp3quran.net:8006/",
            imageUrl = "https://images.unsplash.com/photo-1564507592333-c60657eea523?w=400&q=80",
            category = "قرآن كريم",
            country = "الإذاعة العامة"
        ),
        RadioStation(
            id = "fairouz_radio",
            name = "راديو جارة القمر (فيروزيات)",
            streamUrl = "https://stream.zeno.fm/0vyu3t5zg08uv",
            imageUrl = "https://images.unsplash.com/photo-1494905998402-395d579af36f?w=400&q=80",
            category = "طرب وأغاني",
            country = "لبنان"
        ),
        RadioStation(
            id = "tarab_classic",
            name = "إذاعة طرب أصيل زمان",
            streamUrl = "https://stream.zeno.fm/1v1st2asg8uvw",
            imageUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=400&q=80",
            category = "طرب وأغاني",
            country = "محطة عامة"
        ),
        RadioStation(
            id = "aljazeera_news",
            name = "قناة الجزيرة الإخبارية (صوت مباشر)",
            streamUrl = "https://live-audio.live-streams.nl/aljazeera",
            imageUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=400&q=80",
            category = "أخبار وسياسة",
            country = "قطر"
        ),
        RadioStation(
            id = "bbc_arabic",
            name = "إس بي سي راديو العرب (أغاني شباب)",
            streamUrl = "https://stream.zeno.fm/t640bybteh8uv",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80",
            category = "طرب وأغاني",
            country = "السعودية"
        ),
        RadioStation(
            id = "radio_egypt",
            name = "راديو مصر 88.7 (بث مباشر)",
            streamUrl = "https://stream.zeno.fm/5crfzq3gff8uv",
            imageUrl = "https://images.unsplash.com/photo-1539650116574-8efeb43e2750?w=400&q=80",
            category = "منوعات وأخبار",
            country = "مصر"
        ),
        RadioStation(
            id = "shada_fm",
            name = "إذاعة شدى إف إم (المغرب)",
            streamUrl = "https://shadafm.ice.infomaniak.ch/shadafm-128.mp3",
            imageUrl = "https://images.unsplash.com/photo-1483412033650-1015ddeb83d1?w=400&q=80",
            category = "منوعات وأخبار",
            country = "المغرب"
        ),
        RadioStation(
            id = "ufm_sports",
            name = "إذاعة يو أف أم الرياضية (UFM)",
            streamUrl = "https://stream.zeno.fm/7wtze7bteh8uv",
            imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400&q=80",
            category = "رياضة",
            country = "السعودية"
        )
    )

    // Exposed Flow delivering stations, augmented reactively with favorite statuses from SQLite.
    val stationsFlow: Flow<List<RadioStation>> = radioDao.getAllFavorites().map { favorites ->
        val favoriteIds = favorites.map { it.stationId }.toSet()
        staticStations.map { station ->
            station.copy(isFavorite = favoriteIds.contains(station.id))
        }
    }

    suspend fun toggleFavorite(stationId: String, currentFavorite: Boolean) {
        if (currentFavorite) {
            radioDao.deleteFavoriteById(stationId)
        } else {
            radioDao.insertFavorite(FavoriteStationEntity(stationId))
        }
    }
}
