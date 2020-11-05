package com.sscctv.nursecallapp.service;

import androidx.recyclerview.widget.RecyclerView;

import com.google.protobuf.Empty;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.seeeyes.nursecallapp.calldisplay.CallDisplayGrpc;
import io.seeeyes.nursecallapp.calldisplay.CallPhrase;
import io.seeeyes.nursecallapp.calldisplay.CommonPhrase;
import io.seeeyes.nursecallapp.calldisplay.Location;
import io.seeeyes.nursecallapp.calldisplay.NetworkInfo;
import io.seeeyes.nursecallapp.calldisplay.Reply;
import io.seeeyes.nursecallapp.calldisplay.SystemInfo;
import io.seeeyes.nursecallapp.calldisplay.Time;
import io.seeeyes.nursecallapp.calldisplay.Volume;

public class CallDisplayClient {
    private final ManagedChannel channel;
    //    private final CallDisplayGrpc.CallDisplayStub asyncStub;
    private final CallDisplayGrpc.CallDisplayBlockingStub blockingStub;

    public CallDisplayClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
//        this.asyncStub = CallDisplayGrpc.newStub(channel);
        this.blockingStub = CallDisplayGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public SystemInfo getSystem() {

        Empty request = Empty.newBuilder().build();

        SystemInfo value;

        try {
            value = blockingStub.getSystem(request);
        } catch (StatusRuntimeException e) {
//             gRPC 통신에 문제가 생기면 StatusRuntimeException이 발생
//            Log.d("CallDisplayClient", "onError()");
            return null;
        }

        return value;
    }

// Async stub 예제, 받은 값을 어떻게 activity로 전달할지 모르겠음

//    public void getSystem() {
//
//        Empty request = Empty.newBuilder().build();
//
//        asyncStub.getSystem(request,
//                new StreamObserver<SystemInfo>() {
//                    @Override
//                    public void onNext(SystemInfo value) {
//                        // 여기서는 양방향 스트림이 아니기때문에 정상적인 경우, onNext()가 한번씩만 호출됨
//                        Log.d("CallDisplayClient","Model      : " + value.getModel());
//                        Log.d("CallDisplayClient","Version    : " + value.getVersion());
//                        Log.d("CallDisplayClient","VolumeMax  : " + value.getVolumeMax());
//                        Log.d("CallDisplayClient","VolumeMin  : " + value.getVolumeMin());
//                        Log.d("CallDisplayClient","VolumeStep : " + value.getVolumeStep());
//                        Log.d("CallDisplayClient","VolumeCur  : " + value.getVolumeCur().getVolume());
//                        Log.d("CallDisplayClient","TimeCur    : " + value.getTimeCur().getTime());
//                        Log.d("CallDisplayClient","CommonPhrase : " + value.getCommonPhrase().getPhrase());
//                        Log.d("CallDisplayClient","Network    : " + value.getNetInfoList().toString());
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        // 서버에서 onError()를 호출하거나 내부 통신장애가 발생하면 호출됨
//                        Log.d("CallDisplayClient","onError()");
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        // 메시지 전송이 완료되면 서버에서 onCompleted()를 호출함
//                        Log.d("CallDisplayClient","channel shutdown");
//                        shutdown();
//                    }
//                }
//        );
//    }

    public Reply setNetwork(String ip, String mask, String gate) {

        Reply reply;

        NetworkInfo networkInfo = NetworkInfo.newBuilder()
                .setIf("eth0")
                .setAddr(ip)
                .setMask(mask)
                .setGateway(gate)
                .build();

        try {
            reply = blockingStub.setNetwork(networkInfo);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
//             gRPC 통신에 문제가 생기면 StatusRuntimeException이 발생
//            Log.d("CallDisplayClient", "onError()");
            return null;
        }

        return reply;
    }

// Async stub 예제, 받은 reply 값을 어떻게 activity로 전달해야 할지 모르겠음
//    public void setNetwork() {
//        NetworkInfo networkInfo = NetworkInfo.newBuilder()
//                .setIf("eth0")
//                .setAddr("175.192.153.242")
//                .setMask("255.255.255.0")
//                .setGateway("175.195.153.1")
//                .build();
//
//        asyncStub.setNetwork(networkInfo,
//                new StreamObserver<Reply>() {
//                    @Override
//                    public void onNext(Reply value) {
//                        Log.d("CallDisplayClient","Result : " + value.getResult().toString());
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        Log.d("CallDisplayClient","channel shutdown");
//                        shutdown();
//                    }
//                });
//
//    }

    public Reply setTime() {
        Reply reply;

        Time time = Time.newBuilder()
                .setTime(System.currentTimeMillis())
                .build();

        try {
            reply = blockingStub.setTime(time);

        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }

        return reply;
    }

    public Location getLocation(String loc) {
        Location response;

        Location location = Location.newBuilder()
                .setLoc(loc)
                .build();
        try {
            response = blockingStub.getLocation(location);

        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }

        return response;
    }

    public CommonPhrase getCommonPhrase() {

        return null;
    }

    public Reply setCommonPhrase() {

        return null;
    }

    public Reply displayCallPhrase(String type, String mode, String room, String bed) {
        Reply reply;

        CallPhrase call = CallPhrase.newBuilder()
                .setType(type)
                .setPhrase(mode)
                .setRoom(room)
                .setBed(bed)
                .build();

        try {
            reply = blockingStub.displayCallPhrase(call);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }
        return reply;
    }

    public Reply stopCallPhrase() {
        Reply reply;

        Empty request = Empty.newBuilder().build();

        try {
            reply = blockingStub.stopCallPhrase(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }

        return reply;
    }

    public Reply displayAlarmPhrase() {

        return null;
    }

    public Reply stopAlarmPhrase() {

        return null;
    }

    public Volume getVolume() {

        return null;
    }

    public Reply setVolume() {

        return null;
    }

    public Reply updateApp() {

        return null;
    }
}
