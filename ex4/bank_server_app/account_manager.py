import random, Ice
import Bank
import datetime

class AccountManager(Bank.Account):
  
  def __init__(self, pesel, password, first_name, last_name, monthly_deposit):
    self.pesel = pesel
    self.password = password
    self.first_name = first_name
    self.last_name = last_name
    self.amount = random.random() * 10000 + random.randint(3000, 5000)
    self.monthly_deposit = monthly_deposit
    self.type = Bank.AccountType.STANDARD

  def _authorize(self, credentials):
    creds = Bank.UserCredentials(credentials).pesel
    if creds.pesel != self.pesel or creds.password != self.password:
      raise Bank.UnauthorizedError()

  def _get_type(self):
    return self.type

  def getCurrentState(self, credentials):
    self._authorize(credentials)
    return Bank.AccountInfo(firstName=self.first_name, lastName=self.last_name, amount=self.amount, type=self.type)



class PremiumAccountManager(AccountManager, Bank.PremiumAccount):
  def __init__(self, pesel, password, first_name, last_name, monthly_deposit, currencies, current_ratio_handler):
    AccountManager.__init__(pesel, password, first_name, last_name, monthly_deposit)
    self.type = Bank.AccountType.PREMIUM

  def takeALoan(self, pesel, currency, amount):
    curr = str(currency)
    if curr not in self.currencies():
      raise Bank.LoanRejectionError('Invalid loan currency.')
    elif amount <= 0:
      raise Bank.LoanRejectionError('Invalid loan amount. Must be > 0.')
    return Bank.LoanOffer(currency=currency, amountInPLN=amount * self.current_ratio_handler(curr), amount=amount)