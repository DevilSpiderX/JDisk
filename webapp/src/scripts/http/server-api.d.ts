import { AxiosResponse } from "axios";
export interface Resp {
    code: number,
    msg: string,
    data: any,
    dataCount?: number
}

export interface Http {
    
}

declare const http: Http;

export default http;