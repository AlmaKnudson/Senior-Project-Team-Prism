package app.lights.prism.com.prismlights;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.widget.TextView;

public class DialogCreator {

    public static void cancelShowingDialog(MainActivity activity) {
        Dialog dialog = activity.getDialog();
        //check if it's a progress dialog so we can cancel it safely
        if(dialog != null && dialog.isShowing()) {
            dialog.hide();
        }
    }

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
