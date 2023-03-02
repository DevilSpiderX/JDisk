import { useSystemConfigs } from "@/store/SystemConfigs";

const systemConfigs = useSystemConfigs();

export enum SuffixTypes {
    normal,
    video,
    image,
    audio,
    text,
}

export function getFileSuffixType(suffix: string) {
    if (systemConfigs.values.customVideoSuffix.indexOf(suffix) !== -1) {
        return SuffixTypes.video;
    } else if (systemConfigs.values.customImageSuffix.indexOf(suffix) !== -1) {
        return SuffixTypes.image;
    } else if (systemConfigs.values.customAudioSuffix.indexOf(suffix) !== -1) {
        return SuffixTypes.audio;
    } else if (systemConfigs.values.customTextSuffix.indexOf(suffix) !== -1) {
        return SuffixTypes.text;
    } else {
        return SuffixTypes.normal;
    }
}