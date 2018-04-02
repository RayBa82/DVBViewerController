package org.dvbviewer.controller.player;

import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.flv.FlvExtractor;
import com.google.android.exoplayer2.extractor.mkv.MatroskaExtractor;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.extractor.mp4.FragmentedMp4Extractor;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.extractor.ogg.OggExtractor;
import com.google.android.exoplayer2.extractor.ts.Ac3Extractor;
import com.google.android.exoplayer2.extractor.ts.AdtsExtractor;
import com.google.android.exoplayer2.extractor.ts.PsExtractor;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.extractor.wav.WavExtractor;

import static com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES;
import static com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS;

public class DMSExtractorsFactory implements ExtractorsFactory {

    @Override
    public Extractor[] createExtractors() {
        Extractor[] extractors = new Extractor[11];

        extractors[0] = new MatroskaExtractor(0);
        extractors[1] = new FragmentedMp4Extractor(0);
        extractors[2] = new Mp4Extractor();
        extractors[3] = new Mp3Extractor(0);
        extractors[4] = new AdtsExtractor();
        extractors[5] = new Ac3Extractor();
        extractors[6] = new SeekTsExtractor(FLAG_DETECT_ACCESS_UNITS |FLAG_ALLOW_NON_IDR_KEYFRAMES);
        extractors[7] = new FlvExtractor();
        extractors[8] = new OggExtractor();
        extractors[9] = new PsExtractor();
        extractors[10] = new WavExtractor();

        return extractors;
    }
}
