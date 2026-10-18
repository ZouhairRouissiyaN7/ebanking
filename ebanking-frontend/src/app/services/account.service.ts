import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../environments/environment.development';

import {
  BankAccount,
  AccountHistory,
  DebitRequest,
  CreditRequest,
  TransferRequest,
  TransferResult,
  CreateCurrentAccountRequest,
  CreateSavingAccountRequest,
} from '../models/account.model';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/accounts`;

  getAccounts(): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(this.apiUrl);
  }

  getMyAccounts(): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(`${this.apiUrl}/me`);
  }

  getAccount(id: string): Observable<BankAccount> {
    return this.http.get<BankAccount>(`${this.apiUrl}/${encodeURIComponent(id)}`);
  }

  getCustomerAccounts(customerId: number): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  createCurrentAccount(request: CreateCurrentAccountRequest): Observable<BankAccount> {
    return this.http.post<BankAccount>(`${this.apiUrl}/current`, request);
  }

  createSavingAccount(request: CreateSavingAccountRequest): Observable<BankAccount> {
    return this.http.post<BankAccount>(`${this.apiUrl}/saving`, request);
  }

  getAccountHistory(accountId: string, page: number, size: number): Observable<AccountHistory> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<AccountHistory>(
      `${this.apiUrl}/${encodeURIComponent(accountId)}/operations`,
      { params },
    );
  }

  credit(
    request: CreditRequest,
    requestKey: string = crypto.randomUUID(),
  ): Observable<BankAccount> {
    return this.http.post<BankAccount>(
      `${this.apiUrl}/${encodeURIComponent(request.accountId)}/credit`,
      {
        amount: request.amount,
        description: request.description,
      },
      { headers: this.idempotencyHeaders(requestKey) },
    );
  }

  debit(request: DebitRequest, requestKey: string = crypto.randomUUID()): Observable<BankAccount> {
    return this.http.post<BankAccount>(
      `${this.apiUrl}/${encodeURIComponent(request.accountId)}/debit`,
      {
        amount: request.amount,
        description: request.description,
      },
      { headers: this.idempotencyHeaders(requestKey) },
    );
  }

  transfer(
    request: TransferRequest,
    requestKey: string = crypto.randomUUID(),
  ): Observable<TransferResult> {
    return this.http.post<TransferResult>(`${this.apiUrl}/transfer`, request, {
      headers: this.idempotencyHeaders(requestKey),
    });
  }

  private idempotencyHeaders(requestKey: string): HttpHeaders {
    return new HttpHeaders({
      'Idempotency-Key': requestKey,
    });
  }
}
