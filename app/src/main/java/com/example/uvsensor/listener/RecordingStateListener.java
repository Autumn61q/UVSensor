package com.example.uvsensor.listener;

public interface RecordingStateListener {
    public void onPauseRecording();  // 该函数被调用，说明我们现在要将记录暂停
    public void onContinueRecording();  // 该函数被调用，说明我们现在要开始记录或者是继续记录
    public void onStopRecording();  // 该函数被调用，说明我们现在要讲记录停止

}
