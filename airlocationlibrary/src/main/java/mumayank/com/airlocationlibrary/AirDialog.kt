package mumayank.com.airlocationlibrary

import android.app.Activity
import android.support.v7.app.AlertDialog
import java.lang.ref.WeakReference

class AirDialog {

    class Button(val textOnButton: String, val onClick: () -> Unit)

    companion object {

        var alertDialogBuilder: AlertDialog.Builder? = null

        fun show(
                activity: Activity,
                title: String = "",
                message: String = "",
                iconDrawableId: Int? = null,
                isCancelable: Boolean = true,
                airButton1: Button = Button("OK") {},
                airButton2: Button? = null,
                airButton3: Button? = null
        ) {
            val activityWeakReference = WeakReference(activity)

            alertDialogBuilder = AlertDialog.Builder(activity)

            if (title != "") {
                alertDialogBuilder?.setTitle(title)
            }

            if (message != "") {
                alertDialogBuilder?.setMessage(message)
            }

            if (iconDrawableId != null) {
                alertDialogBuilder?.setIcon(iconDrawableId)
            }

            alertDialogBuilder?.setCancelable(isCancelable)

            alertDialogBuilder?.setPositiveButton(airButton1.textOnButton) { dialogInterface, i ->
                if (activityWeakReference.get() != null) {
                    airButton1.onClick.invoke()
                }
            }

            if (airButton2 != null) {
                alertDialogBuilder?.setNegativeButton(airButton2.textOnButton) { dialogInterface, i ->
                    if (activityWeakReference.get() != null) {
                        airButton2.onClick.invoke()
                    }
                }
            }

            if (airButton3 != null) {
                alertDialogBuilder?.setNeutralButton(airButton3.textOnButton) { dialogInterface, i ->
                    if (activityWeakReference.get() != null) {
                        airButton3.onClick.invoke()
                    }
                }
            }

            if (activity.isFinishing == false) {
                alertDialogBuilder?.show()
            }
        }
    }
}