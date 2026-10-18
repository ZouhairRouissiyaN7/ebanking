import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { forkJoin, Observable, finalize } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';

import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { CustomerService } from '../../services/customer.service';
import { Customer } from '../../models/customer.model';
import { AccountHistory, BankAccount, TransferResult } from '../../models/account.model';

@Component({
  selector: 'app-accounts',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './accounts.html',
})
export class Accounts implements OnInit {
  private readonly accountService = inject(AccountService);
  private readonly authService = inject(AuthService);
  private readonly customerService = inject(CustomerService);
  private readonly fb = inject(FormBuilder);

  readonly isAdmin = computed(() => this.authService.isAdmin());

  accounts = signal<BankAccount[]>([]);
  selectedAccount = signal<BankAccount | null>(null);
  accountHistory = signal<AccountHistory | null>(null);

  loading = signal(false);
  historyLoading = signal(false);
  operationLoading = signal(false);

  errorMessage = signal('');
  successMessage = signal('');

  currentPage = signal(0);
  readonly pageSize = 5;

  searchForm = this.fb.group({
    accountId: ['', Validators.required],
  });

  operationForm = this.fb.group({
    operationType: ['DEBIT', Validators.required],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    description: [''],
    accountDestination: [''],
  });

  operationType = toSignal(this.operationForm.controls.operationType.valueChanges, {
    initialValue: this.operationForm.controls.operationType.value,
  });

  pages = computed(() => {
    const history = this.accountHistory();

    if (!history) return [];

    return Array.from({ length: history.totalPages }, (_, index) => index);
  });

  showCreateForm = signal(false);

  creatingAccount = signal(false);
  loadingCustomers = signal(false);

  customers = signal<Customer[]>([]);

  createError = signal('');
  createSuccess = signal('');

  accountType = signal<'CURRENT' | 'SAVING'>('CURRENT');

  customersLoaded = signal(false);

  createAccountForm = this.fb.group({
    customerId: [null as number | null, [Validators.required, Validators.min(1)]],

    accountType: ['CURRENT' as 'CURRENT' | 'SAVING', Validators.required],

    overdraft: [0, [Validators.required, Validators.min(0)]],

    interestRatePercent: [2.5, [Validators.required, Validators.min(0), Validators.max(100)]],
  });

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    const request$ = this.isAdmin()
      ? this.accountService.getAccounts()
      : this.accountService.getMyAccounts();

    request$.pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);

        const selected = this.selectedAccount();

        if (selected) {
          const updated = accounts.find((account) => account.id === selected.id);

          if (updated) {
            this.selectedAccount.set(updated);
          } else {
            this.selectedAccount.set(null);
            this.accountHistory.set(null);
          }
        }
      },
      error: (err) => {
        this.errorMessage.set(this.getErrorMessage(err, 'Unable to load bank accounts.'));
      },
    });
  }

  selectAccount(account: BankAccount): void {
    this.loadAccountHistory(account.id, 0);
  }

  searchAccount(): void {
    if (this.searchForm.invalid) return;

    const accountId = this.searchForm.controls.accountId.value?.trim();

    if (!accountId) return;

    this.loadAccountHistory(accountId, 0);
  }

  loadAccountHistory(accountId: string, page: number): void {
    this.historyLoading.set(true);
    this.errorMessage.set('');

    forkJoin({
      account: this.accountService.getAccount(accountId),
      history: this.accountService.getAccountHistory(accountId, page, this.pageSize),
    })
      .pipe(finalize(() => this.historyLoading.set(false)))
      .subscribe({
        next: ({ account, history }) => {
          this.selectedAccount.set(account);
          this.accountHistory.set(history);
          this.currentPage.set(page);
        },
        error: (err) => {
          this.errorMessage.set(this.getErrorMessage(err, 'Unable to load account details.'));
        },
      });
  }

  goToPage(page: number): void {
    const account = this.selectedAccount();
    const history = this.accountHistory();

    if (!account || !history) return;

    if (page < 0 || page >= history.totalPages) {
      return;
    }

    this.loadAccountHistory(account.id, page);
  }

  executeOperation(): void {
    const account = this.selectedAccount();

    if (!account || this.operationForm.invalid || this.operationLoading()) {
      return;
    }

    const { operationType, amount, description, accountDestination } =
      this.operationForm.getRawValue();

    if (!operationType || amount == null || amount <= 0) {
      this.errorMessage.set('Enter a valid amount.');
      return;
    }

    const accountId = account.id;
    let request$: Observable<BankAccount | TransferResult>;

    switch (operationType) {
      case 'CREDIT':
        request$ = this.accountService.credit({
          accountId,
          amount,
          description: description ?? '',
        });
        break;

      case 'DEBIT':
        request$ = this.accountService.debit({
          accountId,
          amount,
          description: description ?? '',
        });
        break;

      case 'TRANSFER':
        if (!accountDestination?.trim()) {
          this.errorMessage.set('Destination account ID is required.');
          return;
        }

        request$ = this.accountService.transfer({
          sourceAccountId: accountId,
          destinationAccountId: accountDestination.trim(),
          amount,
          description: description ?? '',
        });
        break;

      default:
        return;
    }

    this.operationLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    request$.pipe(finalize(() => this.operationLoading.set(false))).subscribe({
      next: () => {
        this.successMessage.set(`${operationType} completed successfully.`);

        this.operationForm.reset({
          operationType: 'DEBIT',
          amount: 0,
          description: '',
          accountDestination: '',
        });

        this.loadAccounts();
        this.loadAccountHistory(accountId, 0);
      },
      error: (err) => {
        this.errorMessage.set(this.getErrorMessage(err, 'Banking operation failed.'));
      },
    });
  }

  toggleCreateForm(): void {
    if (!this.isAdmin()) return;

    const opening = !this.showCreateForm();

    this.showCreateForm.set(opening);
    this.createError.set('');
    this.createSuccess.set('');

    if (opening && !this.customersLoaded()) {
      this.loadCustomersForCreation();
    }
  }

  loadCustomersForCreation(): void {
    if (!this.isAdmin()) return;

    this.loadingCustomers.set(true);
    this.createError.set('');

    this.customerService
      .getCustomers()
      .pipe(finalize(() => this.loadingCustomers.set(false)))
      .subscribe({
        next: (customers) => {
          this.customers.set(customers);
          this.customersLoaded.set(true);
        },

        error: (err) => {
          this.createError.set(this.getErrorMessage(err, 'Unable to load customers.'));
        },
      });
  }

  createAccount(): void {
    if (!this.isAdmin() || this.createAccountForm.invalid || this.creatingAccount()) {
      return;
    }

    const form = this.createAccountForm.getRawValue();

    if (form.customerId == null || !form.accountType) {
      return;
    }

    let request$: Observable<BankAccount>;

    if (form.accountType === 'CURRENT') {
      request$ = this.accountService.createCurrentAccount({
        customerId: form.customerId,
        overdraft: form.overdraft ?? 0,
      });
    } else {
      const percentage = form.interestRatePercent ?? 0;

      const interestRate = Number((percentage / 100).toFixed(4));

      request$ = this.accountService.createSavingAccount({
        customerId: form.customerId,
        interestRate,
      });
    }

    this.creatingAccount.set(true);
    this.createError.set('');
    this.createSuccess.set('');

    request$.pipe(finalize(() => this.creatingAccount.set(false))).subscribe({
      next: (account) => {
        this.createSuccess.set(`${account.type} account created successfully.`);

        this.createAccountForm.reset({
          customerId: null,
          accountType: 'CURRENT',
          overdraft: 0,
          interestRatePercent: 2.5,
        });

        this.accountType.set('CURRENT');

        this.showCreateForm.set(false);

        this.loadAccounts();

        this.loadAccountHistory(account.id, 0);
      },

      error: (err) => {
        this.createError.set(this.getErrorMessage(err, 'Unable to create bank account.'));
      },
    });
  }

  onAccountTypeChange(): void {
    const type = this.createAccountForm.controls.accountType.value;

    if (type === 'CURRENT' || type === 'SAVING') {
      this.accountType.set(type);
    }
  }

  private getErrorMessage(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 403) {
      return 'You are not authorized to perform this action.';
    }

    if (error.status === 404) {
      return 'The requested account was not found.';
    }

    if (error.status === 0) {
      return 'Unable to connect to the banking API.';
    }

    return error.error?.detail ?? error.error?.message ?? fallback;
  }
}
