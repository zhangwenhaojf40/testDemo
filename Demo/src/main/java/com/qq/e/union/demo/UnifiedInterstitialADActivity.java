package com.qq.e.union.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;
import com.qq.e.union.demo.adapter.PosIdArrayAdapter;

import java.util.Locale;

import static com.qq.e.union.demo.Constants.VIDEO_DURATION_SETTING_MAX;
import static com.qq.e.union.demo.Constants.VIDEO_DURATION_SETTING_MIN;


public class UnifiedInterstitialADActivity extends Activity implements OnClickListener,
    UnifiedInterstitialADListener, UnifiedInterstitialMediaListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

  private static final String TAG = UnifiedInterstitialADActivity.class.getSimpleName();
  private UnifiedInterstitialAD iad;
  private String currentPosId;

  private CheckBox btnNoOption;
  private CheckBox btnMute;
  private CheckBox btnDetailMute;
  private Spinner networkSpinner;

  private EditText posIdEdt;

  private Spinner spinner;
  private PosIdArrayAdapter arrayAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_unified_interstitial_ad);
    spinner = findViewById(R.id.id_spinner);
    arrayAdapter = new PosIdArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.unified_interstitial));
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(this);

    posIdEdt = findViewById(R.id.posId);
    posIdEdt.setText(PositionId.UNIFIED_VIDEO_PICTURE_ID_LARGE);
    this.findViewById(R.id.loadIAD).setOnClickListener(this);
    this.findViewById(R.id.showIAD).setOnClickListener(this);
    this.findViewById(R.id.showIADAsPPW).setOnClickListener(this);
    this.findViewById(R.id.closeIAD).setOnClickListener(this);
    this.findViewById(R.id.isAdValid).setOnClickListener(this);
    btnNoOption = findViewById(R.id.cb_none_video_option);
    btnNoOption.setOnCheckedChangeListener(this);
    btnMute = findViewById(R.id.btn_mute);
    btnDetailMute = findViewById(R.id.btn_detail_mute);
    networkSpinner = findViewById(R.id.spinner_network);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (iad != null) {
      iad.destroy();
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.loadIAD:
        iad = getIAD();
        setVideoOption();
        iad.loadAD();
        break;
      case R.id.showIAD:
        showAD();
        break;
      case R.id.showIADAsPPW:
        showAsPopup();
        break;
      case R.id.closeIAD:
        close();
        break;
      case R.id.isAdValid:
        isAdValid();
        break;
      default:
        break;
    }
  }

  private void isAdValid() {
    if (iad == null) {
      Toast.makeText(this, "请加载广告后再进行校验 ！ ", Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(this, "广告" + (iad.isValid() ? "有效" : "无效"), Toast.LENGTH_LONG).show();
    }
  }

  private void setVideoOption() {
    VideoOption.Builder builder = new VideoOption.Builder();
    VideoOption option = builder.build();
    if(!btnNoOption.isChecked()){
      option = builder.setAutoPlayMuted(btnMute.isChecked())
          .setAutoPlayPolicy(networkSpinner.getSelectedItemPosition())
          .setDetailPageMuted(btnDetailMute.isChecked())
          .build();
    }
    iad.setVideoOption(option);
    iad.setMinVideoDuration(getMinVideoDuration());
    iad.setMaxVideoDuration(getMaxVideoDuration());

  }

  private UnifiedInterstitialAD getIAD() {
    if (this.iad != null) {
      iad.close();
      iad.destroy();
    }
    String posId = posIdEdt.getText().toString();
    if (!posId.equals(currentPosId) || iad == null) {
      iad = new UnifiedInterstitialAD(this, posId, this);
      currentPosId = posId;
    }
    return iad;
  }

  private void showAD() {
    if (iad != null && iad.isValid()) {
      iad.show();
    } else {
      Toast.makeText(this, "请加载广告后再进行展示 ！ ", Toast.LENGTH_LONG).show();
    }
  }

  private void showAsPopup() {
    if (iad != null && iad.isValid()) {
      iad.showAsPopupWindow();
    } else {
      Toast.makeText(this, "请加载广告后再进行展示 ！ ", Toast.LENGTH_LONG).show();
    }
  }

  private void close() {
    if (iad != null) {
      iad.close();
    } else {
      Toast.makeText(this, "广告尚未加载 ！ ", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onADReceive() {
    Toast.makeText(this, "广告加载成功 ！ ", Toast.LENGTH_LONG).show();
    // onADReceive之后才能调用getAdPatternType()
    if(iad.getAdPatternType() == AdPatternType.NATIVE_VIDEO){
      iad.setMediaListener(this);
    }
    // onADReceive之后才可调用getECPM()
    Log.d(TAG, "eCPMLevel = " + iad.getECPMLevel());
  }

  @Override
  public void onVideoCached() {
    // 视频素材加载完成，在此时调用iad.show()或iad.showAsPopupWindow()视频广告不会有进度条。
    Log.i(TAG, "onVideoCached");
  }

  @Override
  public void onNoAD(AdError error) {
    String msg = String.format(Locale.getDefault(), "onNoAD, error code: %d, error msg: %s",
        error.getErrorCode(), error.getErrorMsg());
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onADOpened() {
    Log.i(TAG, "onADOpened");
  }

  @Override
  public void onADExposure() {
    Log.i(TAG, "onADExposure");
  }

  @Override
  public void onADClicked() {
    Log.i(TAG, "onADClicked : " + (iad.getExt() != null? iad.getExt().get("clickUrl") : ""));
  }

  @Override
  public void onADLeftApplication() {
    Log.i(TAG, "onADLeftApplication");
  }

  @Override
  public void onADClosed() {
    Log.i(TAG, "onADClosed");
  }

  @Override
  public void onVideoInit() {
    Log.i(TAG, "onVideoInit");
  }

  @Override
  public void onVideoLoading() {
    Log.i(TAG, "onVideoLoading");
  }

  @Override
  public void onVideoReady(long videoDuration) {
    Log.i(TAG, "onVideoReady, duration = " + videoDuration);
  }

  @Override
  public void onVideoStart() {
    Log.i(TAG, "onVideoStart");
  }

  @Override
  public void onVideoPause() {
    Log.i(TAG, "onVideoPause");
  }

  @Override
  public void onVideoComplete() {
    Log.i(TAG, "onVideoComplete");
  }

  @Override
  public void onVideoError(AdError error) {
    Log.i(TAG, "onVideoError, code = " + error.getErrorCode() + ", msg = " + error.getErrorMsg());
  }

  @Override
  public void onVideoPageOpen() {
    Log.i(TAG, "onVideoPageOpen");
  }

  @Override
  public void onVideoPageClose() {
    Log.i(TAG, "onVideoPageClose");
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (buttonView == btnNoOption) {
      btnMute.setEnabled(!isChecked);
      btnDetailMute.setEnabled(!isChecked);
      networkSpinner.setEnabled(!isChecked);
    }
  }

  private int getMinVideoDuration() {
    if (((CheckBox) findViewById(R.id.cbMinVideoDuration)).isChecked()) {
      try {
        int rst =
            Integer.parseInt(((EditText) findViewById(R.id.etMinVideoDuration)).getText().toString());
        if (rst > 0) {
          return rst;
        } else {
          Toast.makeText(getApplicationContext(), "最小视频时长输入须大于0!", Toast.LENGTH_LONG).show();
        }
      } catch (NumberFormatException e) {
        Toast.makeText(getApplicationContext(), "最小视频时长输入不是整数!", Toast.LENGTH_LONG).show();
      }
    }
    return 0;
  }

  private int getMaxVideoDuration() {
    if (((CheckBox) findViewById(R.id.cbMaxVideoDuration)).isChecked()) {
      try {
        int rst = Integer.parseInt(((EditText) findViewById(R.id.etMaxVideoDuration)).getText()
            .toString());
        if (rst >= VIDEO_DURATION_SETTING_MIN && rst <= VIDEO_DURATION_SETTING_MAX) {
          return rst;
        } else {
          String msg = String.format("最大视频时长输入不在有效区间[%d,%d]内",
              VIDEO_DURATION_SETTING_MIN, VIDEO_DURATION_SETTING_MAX);
          Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
      } catch (NumberFormatException e) {
        Toast.makeText(getApplicationContext(), "最大视频时长输入不是整数!", Toast.LENGTH_LONG).show();
      }
    }
    return 0;
  }


  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    arrayAdapter.setSelectedPos(position);
    posIdEdt.setText(getResources().getStringArray(R.array.unified_interstitial_value)[position]);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }
}
