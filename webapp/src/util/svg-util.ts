import AudioSvg from "@/assets/file-type-audio.svg";
import BackSvg from "@/assets/file-type-back.svg";
import FileSvg from "@/assets/file-type-file.svg";
import FolderSvg from "@/assets/file-type-folder.svg";
import ImageSvg from "@/assets/file-type-image.svg";
import RootSvg from "@/assets/file-type-root.svg";
import TextSvg from "@/assets/file-type-text.svg";
import VideoSvg from "@/assets/file-type-video.svg";
import { FileTypes } from "@/scripts/file-type";
import { SuffixTypes } from "@/scripts/suffix-type";

export function getSvgByTypeAndSuffix(type: FileTypes, suffixType?: SuffixTypes) {
    if (type === FileTypes.file) {
        if (suffixType === SuffixTypes.video) {
            return VideoSvg;
        } else if (suffixType === SuffixTypes.image) {
            return ImageSvg;
        } else if (suffixType === SuffixTypes.audio) {
            return AudioSvg;
        } else if (suffixType === SuffixTypes.text) {
            return TextSvg;
        } else {
            return FileSvg;
        }
    } else if (type === FileTypes.directory) {
        return FolderSvg;
    } else if (type === FileTypes.back) {
        return BackSvg;
    } else if (type === FileTypes.driver) {
        return RootSvg;
    } else {
        return FileSvg;
    }
}