package com.sscctv.nursecallapp.ui.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerView;
import com.sscctv.nursecallapp.R;
import com.sscctv.nursecallapp.databinding.SettingsAdminBedBinding;
import com.sscctv.nursecallapp.ui.utils.KeyList;
import com.sscctv.nursecallapp.ui.utils.TinyDB;

import org.linphone.core.Core;


public class SettingsAdminBed extends AppCompatActivity {

    private static final String TAG = "SettingsAdminBed";
    private Core core;
    private TinyDB tinyDB;
    private SettingsAdminBedBinding mBinding;
    private int color1, color2, color3, color4, color5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.settings_admin_bed);
        tinyDB = new TinyDB(this);
        int initialColor = 0xFF000000;
        int default_color1 = -10690615;
        int default_color2 = -4760593;
        int default_color3 = -9245824;
        int default_color4 = -936849;
        int default_color5 = -4737868;


        color1 = tinyDB.getInt(KeyList.BED_COLOR_1);
        if (tinyDB.getInt(KeyList.BED_COLOR_1) == 0) {
            color1 = initialColor;
        } else {
            color1 = tinyDB.getInt(KeyList.BED_COLOR_1);
        }

        color2 = tinyDB.getInt(KeyList.BED_COLOR_2);
        if (tinyDB.getInt(KeyList.BED_COLOR_2) == 0) {
            color2 = initialColor;
        } else {
            color2 = tinyDB.getInt(KeyList.BED_COLOR_2);
        }

        color3 = tinyDB.getInt(KeyList.BED_COLOR_3);
        if (tinyDB.getInt(KeyList.BED_COLOR_3) == 0) {
            color3 = initialColor;
        } else {
            color3 = tinyDB.getInt(KeyList.BED_COLOR_3);
        }

        color4 = tinyDB.getInt(KeyList.BED_COLOR_4);
        if (tinyDB.getInt(KeyList.BED_COLOR_4) == 0) {
            color4 = initialColor;
        } else {
            color4 = tinyDB.getInt(KeyList.BED_COLOR_4);
        }

        color5 = tinyDB.getInt(KeyList.BED_COLOR_5);
        if (tinyDB.getInt(KeyList.BED_COLOR_5) == 0) {
            color5 = initialColor;
        } else {
            color5 = tinyDB.getInt(KeyList.BED_COLOR_5);
        }

        mBinding.colorView1.setColor(color1);
        mBinding.colorView2.setColor(color2);
        mBinding.colorView3.setColor(color3);
        mBinding.colorView4.setColor(color4);
        mBinding.colorView5.setColor(color5);

        mBinding.btnColor1.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_color_picker);

            final ColorPickerView colorPickerView = dialog.findViewById(R.id.colorPicker);
            final ColorPanelView colorPanelView = dialog.findViewById(R.id.oldColorView);
            final ColorPanelView newColorPanelView = dialog.findViewById(R.id.newColorView);

            colorPickerView.setOnColorChangedListener(newColor -> {
                newColorPanelView.setColor(colorPickerView.getColor());
            });

            colorPickerView.setColor(color1, true);
            colorPanelView.setColor(color1);

            final Button apply = dialog.findViewById(R.id.okButton);
            apply.setOnClickListener(view1 -> {
                Log.d(TAG, "Color1: " + colorPickerView.getColor());
                mBinding.colorView1.setColor(colorPickerView.getColor());
                tinyDB.putInt(KeyList.BED_COLOR_1, colorPickerView.getColor());
                dialog.dismiss();
            });
            dialog.show();
        });

        mBinding.btnColor2.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_color_picker);

            final ColorPickerView colorPickerView = dialog.findViewById(R.id.colorPicker);
            final ColorPanelView colorPanelView = dialog.findViewById(R.id.oldColorView);
            final ColorPanelView newColorPanelView = dialog.findViewById(R.id.newColorView);

            colorPickerView.setOnColorChangedListener(newColor -> {
                newColorPanelView.setColor(colorPickerView.getColor());
            });

            colorPickerView.setColor(color2, true);
            colorPanelView.setColor(color2);

            final Button apply = dialog.findViewById(R.id.okButton);
            apply.setOnClickListener(view1 -> {
                Log.d(TAG, "Color2: " + colorPickerView.getColor());
                mBinding.colorView2.setColor(colorPickerView.getColor());
                tinyDB.putInt(KeyList.BED_COLOR_2, colorPickerView.getColor());
                dialog.dismiss();
            });
            dialog.show();
        });

        mBinding.btnColor3.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_color_picker);

            final ColorPickerView colorPickerView = dialog.findViewById(R.id.colorPicker);
            final ColorPanelView colorPanelView = dialog.findViewById(R.id.oldColorView);
            final ColorPanelView newColorPanelView = dialog.findViewById(R.id.newColorView);

            colorPickerView.setOnColorChangedListener(newColor -> {
                newColorPanelView.setColor(colorPickerView.getColor());
            });

            colorPickerView.setColor(color3, true);
            colorPanelView.setColor(color3);

            final Button apply = dialog.findViewById(R.id.okButton);
            apply.setOnClickListener(view1 -> {
                Log.d(TAG, "Color3: " + colorPickerView.getColor());
                mBinding.colorView3.setColor(colorPickerView.getColor());
                tinyDB.putInt(KeyList.BED_COLOR_3, colorPickerView.getColor());
                dialog.dismiss();
            });
            dialog.show();
        });

        mBinding.btnColor4.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_color_picker);

            final ColorPickerView colorPickerView = dialog.findViewById(R.id.colorPicker);
            final ColorPanelView colorPanelView = dialog.findViewById(R.id.oldColorView);
            final ColorPanelView newColorPanelView = dialog.findViewById(R.id.newColorView);

            colorPickerView.setOnColorChangedListener(newColor -> {
                newColorPanelView.setColor(colorPickerView.getColor());
            });

            colorPickerView.setColor(color4, true);
            colorPanelView.setColor(color4);

            final Button apply = dialog.findViewById(R.id.okButton);
            apply.setOnClickListener(view1 -> {
                Log.d(TAG, "Color4: " + colorPickerView.getColor());
                mBinding.colorView4.setColor(colorPickerView.getColor());
                tinyDB.putInt(KeyList.BED_COLOR_4, colorPickerView.getColor());
                dialog.dismiss();
            });
            dialog.show();
        });

        mBinding.btnColor5.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_color_picker);

            final ColorPickerView colorPickerView = dialog.findViewById(R.id.colorPicker);
            final ColorPanelView colorPanelView = dialog.findViewById(R.id.oldColorView);
            final ColorPanelView newColorPanelView = dialog.findViewById(R.id.newColorView);

            colorPickerView.setOnColorChangedListener(newColor -> {
                newColorPanelView.setColor(colorPickerView.getColor());
            });

            colorPickerView.setColor(color5, true);
            colorPanelView.setColor(color5);

            final Button apply = dialog.findViewById(R.id.okButton);
            apply.setOnClickListener(view1 -> {
                Log.d(TAG, "Color5: " + colorPickerView.getColor());
                mBinding.colorView5.setColor(colorPickerView.getColor());
                tinyDB.putInt(KeyList.BED_COLOR_5, colorPickerView.getColor());
                dialog.dismiss();
            });
            dialog.show();
        });

        mBinding.btnAllList.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_edit_default);

            final TextView mainTitle = dialog.findViewById(R.id.dialog_main_title);
            final TextView subTitle = dialog.findViewById(R.id.dialog_layout_title);
            final EditText valEdit = dialog.findViewById(R.id.dialog_layout_edit);

            mainTitle.setText("전체 목록 보기 설정");
            subTitle.setText("설정 값");

            String getValue = tinyDB.getString(KeyList.BED_ALL_VIEW);
            Log.d(TAG, "getValue: " + getValue);

            valEdit.setHint(getValue);

            final Button btn = dialog.findViewById(R.id.dialog_edit_button);
            btn.setOnClickListener(view1 -> {
                Log.d(TAG, "Value: " +valEdit.getText().toString());
                tinyDB.putString(KeyList.BED_ALL_VIEW, valEdit.getText().toString());
                dialog.dismiss();
            });
            dialog.show();
        });

        mBinding.btnSelectList.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_edit_default);

            final TextView mainTitle = dialog.findViewById(R.id.dialog_main_title);
            final TextView subTitle = dialog.findViewById(R.id.dialog_layout_title);
            final EditText valEdit = dialog.findViewById(R.id.dialog_layout_edit);

            mainTitle.setText("선택 목록 보기 설정");
            subTitle.setText("설정 값");

            String getValue = tinyDB.getString(KeyList.BED_SELECT_VIEW);
            Log.d(TAG, "getValue: " + getValue);

            valEdit.setHint(getValue);

            final Button btn = dialog.findViewById(R.id.dialog_edit_button);
            btn.setOnClickListener(view1 -> {
                Log.d(TAG, "Value: " +valEdit.getText().toString());
                tinyDB.putString(KeyList.BED_SELECT_VIEW, valEdit.getText().toString());
                dialog.dismiss();
            });
            dialog.show();
        });
    }

//    @Override
//    public void onColorChanged(int newColor) {
//        newColorPanelView.setColor(colorPickerView.getColor());
//    }
}
