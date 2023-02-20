import AudioSvg from "@/assets/file-type-audio.svg";
import BackSvg from "@/assets/file-type-back.svg";
import FileSvg from "@/assets/file-type-file.svg";
import FolderSvg from "@/assets/file-type-folder.svg";
import ImageSvg from "@/assets/file-type-image.svg";
import RootSvg from "@/assets/file-type-root.svg";
import TextSvg from "@/assets/file-type-text.svg";
import VideoSvg from "@/assets/file-type-video.svg";

export function getSvgByTypeAndSuffix(type: "F" | "D" | "driver" | "back",
    suffixType?: "video" | "image" | "audio" | "text" | "normal") {
    if (type === "F") {
        if (suffixType === "video") {
            return VideoSvg;
        } else if (suffixType === "image") {
            return ImageSvg;
        } else if (suffixType === "audio") {
            return AudioSvg;
        } else if (suffixType === "text") {
            return TextSvg;
        } else {
            return FileSvg;
        }
    } else if (type === "D") {
        return FolderSvg;
    } else if (type === "back") {
        return BackSvg;
    } else if (type === "driver") {
        return RootSvg;
    } else {
        return FileSvg;
    }
}