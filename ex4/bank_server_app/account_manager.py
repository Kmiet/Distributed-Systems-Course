import random, Ice
import Bank
import datetime

class AccountManager():
  
  def __init__(self):
    self.accounts = dict()
    self.interest_rate = random.random() / 16
    self.deposit_breakpoint = random.randint(1000, 3000)

  def add_account(self, pesel, monthlyDeposit):
    if monthlyDeposit < 0:
      raise Bank.RegistrationError(reson='Invalid monthly deposit value. Must be >= 0.')
    acc_type = Bank.AccountType.PREMIUM
    if monthlyDeposit < self.deposit_breakpoint:
      acc_type = Bank.AccountType.STANDARD
    self.accounts[pesel] = ((random.random() * 10000 + 500, []), acc_type) 
    return acc_type

  def get_account_state(self, pesel):
    acc_state, _acc_type = self.accounts[pesel]
    return acc_state

  def take_a_loan(self, pesel, currency, amount, returnDate):
    current_date = self._check_return_date(returnDate)
    acc_state, acc_type = self.accounts[pesel]
    if acc_type != Bank.AccountType.PREMIUM:
      raise Bank.LoanRejectionError('Invalid account type')
    value, loans = acc_state
    loans.append(Bank.Loan(
      currency=currency,
      amountTaken=amount,
      amountReturned=0.0,
      interestRate=self.interest_rate,
      takenOn=current_date,
      dueTo=returnDate
    ))
    acc_state = (value, loans)
    self.accounts[pesel] = (acc_state, acc_type)

  def _check_return_date(self, returnDate):
    current_date = datetime.date.today()
    expected_date = datetime.date(current_date.year + 1, current_date.month, current_date.day)
    return_date = datetime.date(returnDate.year, returnDate.month, returnDate.day)
    if return_date < expected_date:
      raise Bank.LoanRejectionError('Invalid returnDate. Must be at least one year from now')
    return Bank.Date(year=current_date.year, month=current_date.month, day=current_date.day)