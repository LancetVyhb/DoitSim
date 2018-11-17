package com.DoIt.CloudAsyncs;

public interface CloudAsyncsListener {
    void onSuccess(long[] id);
    void onFailed(Exception e);
}
