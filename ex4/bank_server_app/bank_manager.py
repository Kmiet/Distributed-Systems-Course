import sys, random, Ice
Ice.loadSlice("../slice_definition/Bank.ice")
import Bank

class BankManager(Bank.User, Bank.Account):
  def __init__(self, user_manager, account_manager, tracker, service_name, service_port):
    Bank.User.__init__(self)
    Bank.Account.__init__(self)
    self.account_manager = account_manager
    self.user_manager = user_manager
    self.currency_tracker = tracker
    self.service_name = service_name
    self.service_port = service_port

    self.deposit_breakpoint = random.randint(1000, 3000)

  def registerNewAcount(self, firstName, lastName, pesel, monthlyDeposit):
    if monthlyDeposit < 0:
      raise Bank.RegistrationError(reson='Invalid monthly deposit value. Must be >= 0.')
    acc_type = Bank.AccountType.PREMIUM
    if monthlyDeposit < self.deposit_breakpoint:
      acc_type = Bank.AccountType.STANDARD
    user = dict(firstName=firstName, lastName=lastName, accountType=acc_type)
    password = self.user_manager.add_user(pesel, user)
    self.account_manager.add_account(pesel, (random.random() * 10000 + 500, []), acc_type)
    return Bank.RegistrationResponse(password=password, accountType=acc_type)

  def getCurrentState(self, credentials):
    pesel, password = credentials
    self.user_manager.verify_credentials(pesel, password)
    state, _type = self.account_manager.get_account_state(pesel)
    value, loans = state
    return Bank.AccountState(value=value, loans=loans)
  
  def takeALoan(self, credentials, currency, amount, returnTime):
    pesel, password = credentials
    self.user_manager.verify_credentials(pesel, password)
    current_date = ''
    if(current_date):
      raise Bank.LoanRejectionError('Invalid return DateTime')
    elif(currency not in self.currency_tracker):
      raise Bank.LoanRejectionError('Invalid loan currency')
    self.account_manager.take_a_loan()
    return Bank.LoanAmount(plnAmount=amount * self.currency_tracker.get_exchange_ratio(currency), foreignCurrencyAmount=amount)

  def start(self):
    with Ice.initialize(sys.argv) as communicator:
      adapter = communicator.createObjectAdapterWithEndpoints("BankAdapter", "default -p " + self.service_port)
      adapter.add(self, communicator.stringToIdentity(self.service_name))
      adapter.activate()
      communicator.waitForShutdown()

  

  