package app.lights.prism.com.prismlights;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.widget.TextView;

/**
 * Allows dialogs to be controlled better with application lifecycle and adds utility methods
 */
public class DialogCreator {

    /**
     * Dismisses the current dialog created with this class or set on the main activity
     * @param activity the main activity
     */
    public static void cancelShowingDialog(MainActivity activity) {
        Dialog dialog = activity.getDialog();
        //check if it's a progress dialog so we can cancel it safely
        if(dialog != null && dialog.isShowing()) {
            dialog.hide();
        }
    }

    /**
     * Cancels the current dialog created with this class or set on the main activity if it's a progress dialog
     * @param activity the main activity
     */
    public static void cancelShowingDialogIfProgress(MainActivity activity) {
        Dialog dialog = activity.getDialog();
        //check if it's a progress dialog so we can cancel it safely
        if(dialog != null && dialog.isShowing() && dialog.findViewById(R.id.progressText) != null) {
            dialog.hide();
        }
    }

    public static void makeShowingDialogCancelable(MainActivity activity) {
        Dialog dialog = activity.getDialog();
        if(dialog != null && dialog.isShowing()) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    public static void setDialogAsShown(Dialog dialog, MainActivity activity) {
        if(activity.getDialog() != null && activity.getDialog().isShowing()) {
            activity.getDialog().hide();
        }
        activity.setDialog(dialog);
    }

    /**
     * Shows a loading dialog with the given loading text and sets it on the main activity
     * @param loadingText the text to be displayed
     * @param activity the main activity
     * @return the dialog it displays
     */
    public static Dialog showLoadingDialog(String loadingText, MainActivity activity) {
        if(activity.getDialog() != null && activity.getDialog().isShowing()) {
            activity.getDialog().hide();
        }
        Dialog dialog = new ProgressDialog(activity);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        activity.setDialog(dialog);
        dialog.show();
        dialog.setContentView(R.layout.progress);
        TextView progressText = (TextView) dialog.findViewById(R.id.progressText);
        progressText.setText(loadingText);
        return dialog;
    }

    /**
     * Shows a warning dialog with the given titleText and explanation and sets it on the main activity
     * @param titleText the text to put in the title of the dialog
     * @param explanation the explanation for the warning
     * @param activity the main activity
     * @return the dialog shown
     */
    public static Dialog showWarningDialog(String titleText, String explanation, MainActivity activity) {
        if(activity.getDialog() != null && activity.getDialog().isShowing()) {
            activity.getDialog().hide();
        }
        ProgressDialog dialog = new ProgressDialog(activity);
        activity.setDialog(dialog);
        dialog.show();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_warning);
        TextView dialogTitle = (TextView) (dialog.findViewById(R.id.dialogTitle));
        dialogTitle.setText(titleText);
        TextView dialogText = (TextView) (dialog.findViewById(R.id.textExplanation));
        dialogText.setText(explanation);
        return dialog;
    }


}
