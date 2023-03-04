package net.crazystar.wechat.service.cognitive;

import com.microsoft.cognitiveservices.speech.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
@Slf4j
public class CognitiveService {
    private final GenericObjectPool<SpeechSynthesizer> pool;

    public CognitiveService(@Value("${azure.cognitive.subscriptionKey}") String subscriptionKey,
                            @Value("${azure.cognitive.region}") String region) {
        GenericObjectPoolConfig<SpeechSynthesizer> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMinIdle(2);
        poolConfig.setMaxTotal(64);

        // Make sure pool is a single object instance used to handle all request.
        // You can put it as a static field of a class
        pool = new GenericObjectPool<>(new SynthesizerPoolFactory(subscriptionKey, region), poolConfig);
    }

    public File tts(String text) throws Exception {
        long start = System.currentTimeMillis();
        File file = new File(UUID.randomUUID().toString().replace("-", "") + ".mp3");
        FileOutputStream outputStream = new FileOutputStream(file);

        SpeechSynthesizer synthesizer = pool.borrowObject();
        SpeechSynthesisResult result = synthesizer.SpeakTextAsync(text).get();
        AudioDataStream audioDataStream = AudioDataStream.fromResult(result);
        // Adjust the buffer size based on your format. You use the buffer size of 50ms audio.
        byte[] buffer = new byte[1600];
        while (true) {
            long len = audioDataStream.readData(buffer);
            if (len == 0) {
                break;
            }
            outputStream.write(buffer);
            // Here you can save the audio or send the data to another pipeline in your service.
        }
        outputStream.close();
        if (audioDataStream.getStatus() != StreamStatus.AllData) {
            SpeechSynthesisCancellationDetails speechSynthesisCancellationDetails = SpeechSynthesisCancellationDetails.fromStream(audioDataStream);
            log.info("{}", speechSynthesisCancellationDetails);
            synthesizer.close();
        } else {
            pool.returnObject(synthesizer);
        }
        audioDataStream.close();
        log.info("tts used time: {}", System.currentTimeMillis() - start);
        return file;
    }


    public static class SynthesizerPoolFactory extends BasePooledObjectFactory<SpeechSynthesizer> {

        private final SpeechConfig config;

        public SynthesizerPoolFactory(String subscriptionKey, String region){
            // Creates an instance of a speech config with specified
            // subscription key and service region. Replace with your own subscription key
            // and service region (e.g., "westus").
            config = SpeechConfig.fromSubscription(subscriptionKey, region);

            // Use a compression format e.g. mp3 to save the bandwidth.
            config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);
            config.setSpeechSynthesisLanguage("zh-CN");
        }

        @Override
        public SpeechSynthesizer create() {
            return new SpeechSynthesizer(config, null);
        }

        @Override
        public PooledObject<SpeechSynthesizer> wrap(SpeechSynthesizer synthesizer) {
            return new DefaultPooledObject<>(synthesizer);
        }

        @Override
        public void destroyObject(PooledObject<SpeechSynthesizer> p) {
            SpeechSynthesizer synthesizer = p.getObject();
            synthesizer.close();
        }
    }
}
