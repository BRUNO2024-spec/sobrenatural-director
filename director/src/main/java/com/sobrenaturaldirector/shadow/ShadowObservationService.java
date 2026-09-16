package com.sobrenaturaldirector.shadow;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Default-off, bounded, observation-only worker. It never blocks the decision caller. */
public final class ShadowObservationService {
    public static final int DEFAULT_QUEUE_CAPACITY=256;
    private final ArrayBlockingQueue<ShadowDecisionSnapshot> queue;
    private final ShadowScorer scorer;
    private final ShadowEventSink sink;
    private final ShadowObservationMetrics metrics=new ShadowObservationMetrics();
    private volatile boolean enabled,stopping;
    private Thread worker;
    public ShadowObservationService(ShadowScorer scorer,ShadowEventSink sink,int capacity){if(capacity<1||scorer==null||sink==null)throw new IllegalArgumentException("invalid shadow service");this.scorer=scorer;this.sink=sink;this.queue=new ArrayBlockingQueue<ShadowDecisionSnapshot>(capacity);}
    public synchronized void start(){if(enabled)return;enabled=true;stopping=false;worker=new Thread(new Runnable(){public void run(){loop();}},"director-shadow-worker");worker.setDaemon(true);worker.start();}
    public synchronized void stop(){enabled=false;stopping=true;if(worker!=null){worker.interrupt();try{worker.join(2000L);}catch(InterruptedException e){Thread.currentThread().interrupt();}}worker=null;queue.clear();}
    public boolean isEnabled(){return enabled;}
    public boolean submit(ShadowDecisionSnapshot snapshot){if(snapshot==null||!enabled||stopping)return false;if(!queue.offer(snapshot)){metrics.dropped();return false;}metrics.submitted();return true;}
    public int getQueueDepth(){return queue.size();} public ShadowObservationMetrics getMetrics(){return metrics;}
    private void loop(){while(!stopping){try{ShadowDecisionSnapshot s=queue.poll(100,TimeUnit.MILLISECONDS);if(s==null)continue;long t=System.nanoTime();ShadowScoreResult r=scorer.score(s);if(r==null)throw new IllegalStateException("null shadow result");sink.accept(r);metrics.scored();}catch(InterruptedException e){if(stopping)break;}catch(RuntimeException e){metrics.error();}}try{sink.close();}catch(RuntimeException ignored){metrics.error();}}
}
