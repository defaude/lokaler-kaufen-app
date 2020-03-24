/**
 * Api Documentation
 * Api Documentation
 *
 * OpenAPI spec version: 1.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *//* tslint:disable:no-unused-variable member-ordering */

import {Inject, Injectable, Optional} from '@angular/core';
import {HttpClient, HttpEvent, HttpHeaders, HttpParams, HttpResponse} from '@angular/common/http';
import {CustomHttpUrlEncodingCodec} from '../encoder';

import {Observable} from 'rxjs';

import {LocationSuggestionsDto} from '../model/locationSuggestionsDto';

import {BASE_PATH} from '../variables';
import {Configuration} from '../configuration';


@Injectable()
export class LocationControllerService {

    protected basePath = '//localhost:4200';
    public defaultHeaders = new HttpHeaders();
    public configuration = new Configuration();

    constructor(protected httpClient: HttpClient, @Optional()@Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
        if (basePath) {
            this.basePath = basePath;
        }
        if (configuration) {
            this.configuration = configuration;
            this.basePath = basePath || configuration.basePath || this.basePath;
        }
    }

    /**
     * @param consumes string[] mime-types
     * @return true: consumes contains 'multipart/form-data', false: otherwise
     */
    private canConsumeForm(consumes: string[]): boolean {
        const form = 'multipart/form-data';
        for (const consume of consumes) {
            if (form === consume) {
                return true;
            }
        }
        return false;
    }


    /**
     * getSuggestions
     *
     * @param zipCode zipCode
     * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
     * @param reportProgress flag to report request and response progress.
     */
    public getSuggestionsUsingGET(zipCode: string, observe?: 'body', reportProgress?: boolean): Observable<LocationSuggestionsDto>;
    public getSuggestionsUsingGET(zipCode: string, observe?: 'response', reportProgress?: boolean): Observable<HttpResponse<LocationSuggestionsDto>>;
    public getSuggestionsUsingGET(zipCode: string, observe?: 'events', reportProgress?: boolean): Observable<HttpEvent<LocationSuggestionsDto>>;
    public getSuggestionsUsingGET(zipCode: string, observe: any = 'body', reportProgress: boolean = false ): Observable<any> {

        if (zipCode === null || zipCode === undefined) {
            throw new Error('Required parameter zipCode was null or undefined when calling getSuggestionsUsingGET.');
        }

        let queryParameters = new HttpParams({encoder: new CustomHttpUrlEncodingCodec()});
        if (zipCode !== undefined && zipCode !== null) {
            queryParameters = queryParameters.set('zipCode', <any>zipCode);
        }

        let headers = this.defaultHeaders;

        // to determine the Accept header
        let httpHeaderAccepts: string[] = [
            'application/json'
        ];
        const httpHeaderAcceptSelected: string | undefined = this.configuration.selectHeaderAccept(httpHeaderAccepts);
        if (httpHeaderAcceptSelected != undefined) {
            headers = headers.set('Accept', httpHeaderAcceptSelected);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        return this.httpClient.request<LocationSuggestionsDto>('get',`${this.basePath}/api/location/suggestion`,
            {
                params: queryParameters,
                withCredentials: this.configuration.withCredentials,
                headers: headers,
                observe: observe,
                reportProgress: reportProgress
            }
        );
    }

}
