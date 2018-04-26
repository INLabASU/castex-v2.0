package rte

import android.graphics.Bitmap
import java.math.BigInteger

/**
 * Created by jk on 2/26/18.
 * Just a filler class for now, may be necessary for future use by adding timestamps.
 */
data class RTEFrame(var bitmap: Bitmap, var fid: Long, var timestamp: BigInteger)