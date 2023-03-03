<script setup lang="ts">
import { computed, ref, watch, watchEffect } from 'vue';

interface Props {
    name?: string,
    src?: string,
    currentTime?: number,
    duration?: number,
    preload?: "auto" | "metadata" | "none",
    autoplay?: boolean,
}

const props = withDefaults(defineProps<Props>(), {
    name: "",
    preload: "auto",
    autoplay: false,
});

const emit = defineEmits<{
    (e: 'update:currentTime', currentTime: number): void,
    (e: 'update:duration', duration: number): void,
}>();

const loading = ref(false);

const audioRef = ref<HTMLAudioElement | null>(null);

function play() {
    if (audioRef.value && audioRef.value.readyState > 0) {
        audioRef.value?.play();
    }
}

function pause() {
    audioRef.value?.pause();
}

watchEffect(() => {
    if (props.src && props.src !== "" && audioRef.value) {
        loading.value = true;
    }
});

const defaultCurrentTime = ref(0);

const _currentTime = computed({
    get: () => props.currentTime ?? defaultCurrentTime.value,
    set: newValue => {
        defaultCurrentTime.value = newValue;
        emit("update:currentTime", newValue);
    }
});

const timeoutId = ref<NodeJS.Timeout>();

function setCurrentTime(sec: number) {
    clearTimeout(timeoutId.value);
    timeoutId.value = setTimeout(() => {
        if (audioRef.value) {
            audioRef.value.currentTime = sec;
            _currentTime.value = sec;
            timeoutId.value = undefined;
        }
    }, 300);
}

const defaultDuration = ref(1);

const _duration = computed({
    get: () => props.duration ?? defaultDuration.value,
    set: newValue => {
        defaultDuration.value = newValue;
        emit("update:duration", newValue);
    }
});

function on_time_update() {
    if (audioRef.value) {
        _currentTime.value = audioRef.value.currentTime;
    }
}

function on_duration_change() {
    if (audioRef.value) {
        _duration.value = audioRef.value.duration;
    }
}

function on_loaded_metadata() {
    loading.value = false;
    if (audioRef.value) {
        audioRef.value.currentTime = 0;
    }
}

const paused = ref(true);

watchEffect(() => {
    if (props.src === undefined || props.src === "") {
        paused.value = true;
    }
});

function on_playbutton_click() {
    if (paused.value) {
        play()
    } else {
        pause();
    }
}

function secToTime(sec: number) {
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s < 10 ? '0' + s : s}`;
}

const muted = ref(false);
let tempAfterMute = 0;
function on_muteButton_click() {
    if (muted.value) {
        muted.value = false;
        volumeProgress.value = tempAfterMute;
    } else {
        muted.value = true;
        tempAfterMute = volumeProgress.value;
        volumeProgress.value = 0;
    }
}

const volume = ref(1);

const volumeProgress = ref(100);

watchEffect(() => {
    if (volumeProgress.value > 100) {
        volumeProgress.value = 100;
    } else if (volumeProgress.value < 0) {
        volumeProgress.value = 0;
    }
    volume.value = Math.floor(volumeProgress.value) / 100;
});

function setVolumeProgress(value: number) {
    muted.value = false;
    volumeProgress.value = value;
}

function on_volume_slider_change(value: number | [number, number]) {
    if (typeof value === "number") {
        setVolumeProgress(value);
    }
}

function on_volume_wrapper_wheel(ev: WheelEvent) {
    setVolumeProgress(volumeProgress.value - (ev.deltaY > 0 ? 2 : -2));
}

const volumeBarVisible = ref(false);

let lastVolumeSliderTouches: TouchList | undefined = undefined;

function on_volume_slider_touchstart(ev: TouchEvent) {
    lastVolumeSliderTouches = ev.touches;
}

function on_volume_slider_touchend() {
    lastVolumeSliderTouches = undefined
}

function on_volume_slider_touchmove(ev: TouchEvent) {
    const touches = ev.touches;
    if (lastVolumeSliderTouches) {
        const newY = touches[0].clientY;
        const oldY = lastVolumeSliderTouches[0].clientY;
        const neg = newY > oldY;
        const moveY = Math.abs(newY - oldY);
        if (neg) {
            setVolumeProgress(volumeProgress.value - moveY);
        } else {
            setVolumeProgress(volumeProgress.value + moveY);
        }
    }
    lastVolumeSliderTouches = touches;
}

const playProgressTemp = ref<number | undefined>(undefined);

watch(timeoutId, id => {
    if (id === undefined) {
        playProgressTemp.value = undefined;
    }
});

const playProgress = computed({
    get: () => playProgressTemp.value ?? _currentTime.value,
    set: newValue => {
        let value = newValue;
        if (value < 0) {
            value = 0;
        } else if (value > _duration.value) {
            value = _duration.value;
        }
        playProgressTemp.value = value;
        setCurrentTime(value);
    }
});

let lastPlaySliderTouches: TouchList | undefined = undefined;

function on_play_slider_touchstart(ev: TouchEvent) {
    lastPlaySliderTouches = ev.touches;
}

function on_play_slider_touchend() {
    lastPlaySliderTouches = undefined
}

function on_play_slider_touchmove(ev: TouchEvent) {
    const touches = ev.touches;
    const px2Sec = _duration.value / 300;
    if (lastPlaySliderTouches) {
        const newX = touches[0].clientX;
        const oldX = lastPlaySliderTouches[0].clientX;
        const neg = newX < oldX;
        const moveX = Math.abs(newX - oldX);
        if (neg) {
            playProgress.value -= (moveX * px2Sec);
        } else {
            playProgress.value += (moveX * px2Sec);
        }
    }
    lastPlaySliderTouches = touches;
}

function on_audio_player_keydown(ev: KeyboardEvent) {
    const key = ev.key;
    if (key === "ArrowUp") {
        volumeBarVisible.value = true;
        setVolumeProgress(volumeProgress.value + 1);
    } else if (key === "ArrowDown") {
        volumeBarVisible.value = true;
        setVolumeProgress(volumeProgress.value - 1);
    } else if (key === "ArrowRight") {
        volumeBarVisible.value = false;
        playProgress.value += 1;
    } else if (key === "ArrowLeft") {
        volumeBarVisible.value = false;
        playProgress.value -= 1;
    } else if (key === " ") {
        on_playbutton_click();
    }
}

</script>

<template>
    <ASpin :loading="loading">
        <div class="audio-outer" tabindex="-1" @keydown="on_audio_player_keydown">
            <audio ref="audioRef" v-bind="{ src, autoplay, muted, volume }" @play="paused = false" @pause="paused = true"
                @timeupdate="on_time_update" @durationchange="on_duration_change" @loadedmetadata="on_loaded_metadata" />

            <AGrid class="audio-wrapper" :cols="8" :col-gap="10" :row-gap="10">
                <AGridItem class="grid-item" :span="8">{{ name }}</AGridItem>

                <AGridItem class="grid-item item-center " :span="2">
                    <AButton shape="circle" @click="on_playbutton_click" @contextmenu.prevent="on_playbutton_click">
                        <template #icon>
                            <i v-if="paused" class="fa-solid fa-play"></i>
                            <i v-else class="fa-solid fa-pause"></i>
                        </template>
                    </AButton>
                </AGridItem>

                <AGridItem class="grid-item item-center  time-display" :span="4">
                    {{ secToTime(playProgressTemp ?? _currentTime) }} / {{ secToTime(_duration) }}
                </AGridItem>

                <AGridItem class="grid-item item-center " :span="2">
                    <ATrigger position="top" auto-fit-position v-model:popupVisible="volumeBarVisible">
                        <AButton shape="circle" @click="on_muteButton_click" @contextmenu.prevent="volumeBarVisible = true"
                            @wheel.stop.prevent="on_volume_wrapper_wheel">
                            <template #icon>
                                <!-- 静音标记 -->
                                <i v-if="muted" class="fa-solid fa-volume-slash"></i>
                                <!-- 音量为0 -->
                                <i v-else-if="volume === 0" class="fa-solid fa-volume-off"></i>
                                <!-- 音量低 -->
                                <i v-else-if="volume <= 0.4" class="fa-solid fa-volume-low"></i>
                                <!-- 音量中 -->
                                <i v-else-if="volume <= 0.8" class="fa-solid fa-volume"></i>
                                <!-- 音量高 -->
                                <i v-else class="fa-solid fa-volume-high"></i>
                            </template>
                        </AButton>
                        <template #content>
                            <div class="volume-wrapper" @wheel.stop.prevent="on_volume_wrapper_wheel"
                                @touchstart="on_volume_slider_touchstart" @touchmove.prevent="on_volume_slider_touchmove"
                                @touchend="on_volume_slider_touchend">
                                <ASlider class="volume-slider" v-model="volumeProgress" direction="vertical"
                                    :show-tooltip="false" @change="on_volume_slider_change" />
                            </div>
                        </template>
                    </ATrigger>
                </AGridItem>

                <AGridItem class="grid-item" :span="8">
                    <div class="progress-slider-outer item-center" @touchstart="on_play_slider_touchstart"
                        @touchmove.prevent="on_play_slider_touchmove" @touchend="on_play_slider_touchend">
                        <ASlider v-model="playProgress" :max="_duration" :show-tooltip="false" />
                    </div>
                </AGridItem>
            </AGrid>
        </div>
    </ASpin>
</template>

<style scoped>
.audio-outer {
    width: 300px;
    height: 116px;
    padding: 20px;
    background-color: var(--color-bg-2);
    border: 1px solid var(--color-border-2);
    border-radius: var(--border-radius-medium);
}

.audio-outer:focus-visible {
    outline: 0;
}

.audio-wrapper {
    width: 100%;
    height: 100%;
}

.grid-item {
    height: 32px;
}

.volume-wrapper {
    padding: 12px 3px;
    background-color: var(--color-bg-3);
    border: 1px solid var(--color-border-3);
    border-radius: 1rem;
}

.volume-slider {
    height: 100px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.volume-slider :deep(.arco-slider-track.arco-slider-track-vertical) {
    min-height: 100px;
    margin: 0;
}

.item-center {
    display: flex;
    justify-content: center;
    align-items: center;
}

.time-display {
    font-size: 18px;
}

.progress-slider-outer {
    width: 100%;
    height: 100%;
}
</style>