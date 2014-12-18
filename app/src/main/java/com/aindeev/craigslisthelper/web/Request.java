package com.aindeev.craigslisthelper.web;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.Map;

/**
 * Created by aindeev on 14-12-09.
 */
public abstract class Request<T> extends AsyncHttpResponseHandler {

    public enum RequestType {
        GET,
        POST
    };

    protected boolean isExecuting = false;
    protected RequestCallback<T> asyncCallback = null;
    private RequestType type;

    public Request(RequestType type) {
        this.type = type;
    }

    public abstract String getUrl();
    public abstract Header[] getHeaders();

    public abstract Map<String,String> getStaticParams();
    public abstract RequestParams getParams();

    public void addStaticParams(RequestParams params) {
        for (Map.Entry<String, String> entry : getStaticParams().entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }
    }

    public void doPost(boolean async) {
        CraigslistClient.instance().doPost(async, getUrl(), getHeaders(), getParams(), this);
    }

    public void doGet(boolean async) {
        CraigslistClient.instance().doGet(async, getUrl(), getHeaders(), getParams(), this);
    }

    public abstract void onRequestSuccess(int i, Header[] headers, byte[] bytes);
    public abstract void onRequestFailure(int i, Header[] headers, byte[] bytes);

    @Override
    public void onSuccess(int i, Header[] headers, byte[] bytes) {
        onRequestSuccess(i, headers, bytes);
        if (asyncCallback != null)
            asyncCallback.onRequestDone(getResult());
        isExecuting = false;
    }
    @Override
    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
        onRequestFailure(i, headers, bytes);
        if (asyncCallback != null)
            asyncCallback.onRequestDone(getResult());
        isExecuting = false;
    }

    public abstract void beforeRequest();
    public abstract T getResult();

    public T execute() throws IllegalStateException {
        if (isExecuting)
            throw new IllegalStateException("This request is currently executing");
        else
            isExecuting = true;

        beforeRequest();
        if (type == RequestType.POST)
            this.doPost(false);
        else
            this.doGet(false);

        return getResult();
    }

    public void execute(RequestCallback<T> callback) throws IllegalStateException {
        if (isExecuting)
            throw new IllegalStateException("This request is currently executing");
        else
            isExecuting = true;

        this.asyncCallback = callback;
        beforeRequest();

        if (type == RequestType.POST)
            this.doPost(true);
        else
            this.doGet(true);
    }

}
