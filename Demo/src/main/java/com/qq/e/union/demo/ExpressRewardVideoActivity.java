package com.qq.e.union.demo;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.ads.rewardvideo2.ExpressRewardVideoAD;
import com.qq.e.ads.rewardvideo2.ExpressRewardVideoAdListener;
import com.qq.e.comm.util.AdError;
import com.qq.e.comm.util.VideoAdValidity;
import com.qq.e.union.demo.adapter.PosIdArrayAdapter;

import java.util.Map;

/**
 * 注意：需要在开发者平台创建模版激励视频广告位，激励视频广告位不会返回广告!!!
 */
public class ExpressRewardVideoActivity extends Activity {

  private static final String TAG = ExpressRewardVideoActivity.class.getSimpleName();

  private ExpressRewardVideoAD mRewardVideoAD;
  private EditText mPosIdEt;
  private String mPosId;
  private boolean mIsLoaded;
  private boolean mIsCached;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_express_reward_video);
    mPosIdEt = findViewById(R.id.position_id);
    mPosId = getPosID();
    Spinner spinner = findViewById(R.id.id_spinner);
    PosIdArrayAdapter arrayAdapter = new PosIdArrayAdapter(this, android.R.layout.simple_spinner_item,
        getResources().getStringArray(R.array.express_reward_video));
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        arrayAdapter.setSelectedPos(position);
        mPosId = getResources().getStringArray(R.array.express_reward_video_value)[position];
        mPosIdEt.setText(mPosId);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {/* do nothing*/}
    });
  }

  private void initAdManager(String posId) {
    if (mRewardVideoAD != null) {
      mRewardVideoAD.destroy();
    }
    mRewardVideoAD = new ExpressRewardVideoAD(this, posId, new ExpressRewardVideoAdListener() {
      @Override
      public void onAdLoaded() {
        mIsLoaded = true;
        Log.i(TAG,
            "onAdLoaded: VideoDuration " + mRewardVideoAD.getVideoDuration() + ", ECPMLevel " +
                mRewardVideoAD.getECPMLevel());
        showToast("广告拉取成功");
      }

      @Override
      public void onVideoCached() {
        // 在视频缓存完成之后再进行广告展示，以保证用户体验
        mIsCached = true;
        Log.i(TAG, "onVideoCached: ");
      }

      @Override
      public void onShow() {
        Log.i(TAG, "onShow: ");
      }

      @Override
      public void onExpose() {
        Log.i(TAG, "onExpose: ");
      }

      /**
       * 模板激励视频触发激励
       *
       * @param map 若选择了服务端验证，可以通过 ServerSideVerificationOptions#TRANS_ID 键从 map 中获取此次交易的 id；若未选择服务端验证，则不需关注 map 参数。
       */
      @Override
      public void onReward(Map<String, Object> map) {
        Object o = map.get(ServerSideVerificationOptions.TRANS_ID); // 获取服务端验证的唯一 ID
        Log.i(TAG, "onReward " + o);
      }

      @Override
      public void onClick() {
        Log.i(TAG, "onClick: ");
      }

      @Override
      public void onVideoComplete() {
        Log.i(TAG, "onVideoComplete: ");
      }

      @Override
      public void onClose() {
        Log.i(TAG, "onClose: ");
      }

      @Override
      public void onError(AdError error) {
        showToast("广告错误: " + error.getErrorMsg());
        Log.i(TAG, "onError: code = " + error.getErrorCode() + " msg = " + error.getErrorMsg());
      }
    });
  }


  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.load_ad_button:
        boolean volumeOn = ((CheckBox) findViewById(R.id.volume_on_checkbox)).isChecked();
        if (mPosId != getPosID()) {
          mPosId = getPosID();
          initAdManager(mPosId);
        }
        mRewardVideoAD.setVolumeOn(volumeOn);
        ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
            .setCustomData("APP's custom data") // 设置激励视频服务端验证的自定义信息
            .setUserId("APP's user id for server verify") // 设置服务端验证的用户信息
            .build();
        mRewardVideoAD.setServerSideVerificationOptions(options);
        mRewardVideoAD.loadAD();
        mIsLoaded = false;
        mIsCached = false;
        break;
      case R.id.show_ad_button:
      case R.id.show_ad_button_activity:
        if (mRewardVideoAD == null || !mIsLoaded) {
          showToast("广告未拉取成功！");
          return;
        }
        VideoAdValidity validity = mRewardVideoAD.checkValidity();
        switch (validity) {
          case SHOWED:
          case OVERDUE:
            showToast(validity.getMessage());
            Log.i(TAG, "onClick: " + validity.getMessage());
            return;
          // 在视频缓存成功后展示，以省去用户的等待时间，提升用户体验
          case NONE_CACHE:
            showToast("广告素材未缓存成功！");
//            return;
          case VALID:
            Log.i(TAG, "onClick: " + validity.getMessage());
            // 展示广告
            break;
        }
        // 在视频缓存成功后展示，以省去用户的等待时间，提升用户体验
        mRewardVideoAD
            .showAD(view.getId() == R.id.show_ad_button ? null : ExpressRewardVideoActivity.this);
        break;
    }
  }

  private void showToast(String msg) {
    Toast.makeText(ExpressRewardVideoActivity.this, msg, Toast.LENGTH_SHORT).show();
  }

  private String getPosID() {
    return mPosIdEt.getText().toString();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mRewardVideoAD != null) {
      mRewardVideoAD.destroy();
    }
  }
}
