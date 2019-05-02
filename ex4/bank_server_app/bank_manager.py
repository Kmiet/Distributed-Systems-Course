from threading import Thread
import sys, random, Ice
import Bank

class BankManager(Bank.Client):
  def __init__(self, user_manager, account_manager, tracker, service_name, service_port):
    self.account_manager = account_manager
    self.user_manager = user_manager
    self.currency_tracker = tracker
    self.service_name = service_name
    self.service_port = service_port

  def registerNewAccount(self, firstName, lastName, pesel, monthlyDeposit, current=None):
    user = dict(pesel=pesel, firstName=firstName, lastName=lastName)
    password = self.user_manager.add_user(pesel, user)
    acc_type = self.account_manager.add_account(pesel, monthlyDeposit)
    return Bank.RegistrationResponse(password=password, accountType=acc_type)

  def getCurrentState(self, credentials, current=None):
    creds = Bank.UserCredentials(credentials).pesel
    self.user_manager.verify_credentials(creds.pesel, creds.password)
    state = self.account_manager.get_account_state(creds.pesel)
    value, loans = state
    return Bank.AccountState(value=value, loans=loans)
  
  def takeALoan(self, credentials, currency, amount, returnDate, current=None):
    creds = Bank.UserCredentials(credentials).pesel
    self.user_manager.verify_credentials(creds.pesel, creds.password)
    curr = str(currency)
    if curr not in self.currency_tracker.get_currencies():
      raise Bank.LoanRejectionError('Invalid loan currency')
    elif amount <= 0:
      raise Bank.LoanRejectionError('Invalid loan amount. Must be > 0')
    self.account_manager.take_a_loan(creds.pesel, curr, amount, returnDate)
    return Bank.LoanAmount(plnAmount=amount * self.currency_tracker.get_exchange_ratio(curr), foreignCurrencyAmount=amount)

  def _run(self):
    with Ice.initialize(sys.argv) as communicator:
      adapter = communicator.createObjectAdapterWithEndpoints("BankAdapter", "default -p " + self.service_port)
      adapter.add(self, communicator.stringToIdentity("BankAdapter"))
      adapter.activate()

      self.adapter = adapter
      communicator.waitForShutdown()

  def start(self):
    bm = Thread(target=self._run)
    bm.start()
    self.currency_tracker.subscribe()
    bm.join()
