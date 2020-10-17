package edu.wpi.cs528project3;

// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;

/**
 * Utility class for access to runtime permissions.
 */
public abstract class PermissionUtils {

    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */
    public static void requestPermission(AppCompatActivity activity, int requestId,
                                         String permission, boolean finishActivity,
                                         @StringRes int permissionRequestedResID,
                                         @StringRes int rationaleResID) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Display a dialog with rationale.
            PermissionUtils.RationaleDialog.newInstance(permission, requestId, finishActivity,
                    permissionRequestedResID, rationaleResID)
                    .show(activity.getSupportFragmentManager(), "dialog");
        } else {
            // Location permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestId);

        }
    }

    /**
     * Checks if the result contains a {@link PackageManager#PERMISSION_GRANTED} result for a
     * permission from a runtime permissions request.
     *
     * @see androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    /**
     * A dialog that displays a permission denied message.
     */
    public static class PermissionDeniedDialog extends DialogFragment {

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private static final String ARGUMENT_PERMISSION_REQUIRED_RESOURCE_ID = "permissionRequiredResourceID";

        private static final String ARGUMENT_PERMISSION_DENIED_RESOURCE_ID = "permissionDeniedResourceID";

        private boolean finishActivity = false;

        private int permissionRequiredResourceID;
        private int permissionDeniedResourceID;

        /**
         * Creates a new instance of this dialog and optionally finishes the calling Activity
         * when the 'Ok' button is clicked.
         */
        public static PermissionDeniedDialog newInstance(boolean finishActivity,
                                                  @StringRes int permissionDeniedResID,
                                                  @StringRes int permissionRequiredResID) {
            Bundle arguments = new Bundle();
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
            arguments.putInt(ARGUMENT_PERMISSION_DENIED_RESOURCE_ID, permissionDeniedResID);
            arguments.putInt(ARGUMENT_PERMISSION_REQUIRED_RESOURCE_ID, permissionRequiredResID);

            PermissionDeniedDialog dialog = new PermissionDeniedDialog(permissionRequiredResID);
            dialog.setArguments(arguments);
            return dialog;
        }

        private PermissionDeniedDialog(@StringRes int permissionDeniedResID) {
            permissionDeniedResourceID = permissionDeniedResID;
        }

        @NonNull @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            finishActivity = getArguments().getBoolean(ARGUMENT_FINISH_ACTIVITY);
            permissionRequiredResourceID = getArguments().getInt(ARGUMENT_PERMISSION_REQUIRED_RESOURCE_ID);
            permissionDeniedResourceID = getArguments().getInt(ARGUMENT_PERMISSION_DENIED_RESOURCE_ID);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(permissionDeniedResourceID)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(getActivity(), permissionRequiredResourceID,
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    /**
     * A dialog that explains the use of the location permission and requests the necessary
     * permission.
     * <p>
     * The activity should implement
     * {@link androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback}
     * to handle permit or denial of this permission request.
     */
    public static class RationaleDialog extends DialogFragment {

        private static final String ARGUMENT_PERMISSION = "permission";

        private static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private static final String ARGUMENT_PERMISSION_REQUIRED_RESOURCE_ID = "permissionRequiredResourceID";

        private static final String ARGUMENT_PERMISSION_RATIONALE_RESOURCE_ID = "rationaleResourceID";

        private boolean finishActivity = false;

        private int permissionRequiredResourceID;
        private int rationaleResourceID;

        /**
         * Creates a new instance of a dialog displaying the rationale for the use of the
         * permission.
         * <p>
         * The permission is requested after clicking 'ok'.
         *
         * @param requestCode    Id of the request that is used to request the permission. It is
         *                       returned to the
         *                       {@link androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback}.
         * @param finishActivity Whether the calling Activity should be finished if the dialog is
         *                       cancelled.
         */
        public static RationaleDialog newInstance(String permission, int requestCode,
                                                  boolean finishActivity,
                                                  @StringRes int permissionRequiredResID,
                                                  @StringRes int rationaleResID) {
            Bundle arguments = new Bundle();
            arguments.putString(ARGUMENT_PERMISSION, permission);
            arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode);
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
            arguments.putInt(ARGUMENT_PERMISSION_REQUIRED_RESOURCE_ID, permissionRequiredResID);
            arguments.putInt(ARGUMENT_PERMISSION_RATIONALE_RESOURCE_ID, rationaleResID);
            RationaleDialog dialog = new RationaleDialog(rationaleResID);
            dialog.setArguments(arguments);
            return dialog;
        }

        private RationaleDialog(@StringRes int rationaleResID) {
            rationaleResourceID = rationaleResID;
        }

        @NonNull @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            final String permission = arguments.getString(ARGUMENT_PERMISSION);
            final int requestCode = arguments.getInt(ARGUMENT_PERMISSION_REQUEST_CODE);
            finishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY);
            permissionRequiredResourceID = arguments.getInt(ARGUMENT_PERMISSION_REQUIRED_RESOURCE_ID);
            rationaleResourceID = arguments.getInt(ARGUMENT_PERMISSION_RATIONALE_RESOURCE_ID);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(rationaleResourceID)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // After click on Ok, request the permission.
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{permission},
                                    requestCode);
                            // Do not finish the Activity while requesting permission.
                            finishActivity = false;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(getActivity(),
                        permissionRequiredResourceID,
                        Toast.LENGTH_SHORT)
                        .show();
                getActivity().finish();
            }
        }
    }
}