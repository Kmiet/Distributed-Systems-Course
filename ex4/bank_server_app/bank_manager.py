from threading import Thread
import sys, random, Ice
sys.path.append('./ice_out')
import Bank
from account_manager import AccountManager, PremiumAccountManager

class BankManager(Bank.User):
  def __init__(self, user_manager, tracker, service_name, service_port):
    self.accounts = dict()
    self.user_manager = user_manager
    self.currency_tracker = tracker
    self.service_name = service_name
    self.service_port = service_port
    self.deposit_breakpoint = random.randint(1000, 3000)

  def registerNewAccount(self, firstName, lastName, pesel, monthlyDeposit, current=None):
    password = self.user_manager.add_user(pesel)
    if monthlyDeposit < 0:
      raise Bank.RegistrationError(reson='Invalid monthly deposit value. Must be >= 0.')
    if monthlyDeposit < self.deposit_breakpoint:
      acc_type = Bank.AccountType.STANDARD
      acc = AccountManager(
        pesel, 
        password, 
        firstName, 
        lastName, 
        monthlyDeposit
      )
    else:
      acc_type = Bank.AccountType.PREMIUM
      acc = PremiumAccountManager(
        pesel, 
        password, 
        firstName, 
        lastName, 
        monthlyDeposit, 
        self.currency_tracker.get_currencies, 
        self.currency_tracker.get_exchange_ratio
      )
    
    self.adapter.add(acc, self.communicator.stringToIdentity(pesel + str(acc_type)))
    self.accounts[pesel] = acc

    base = current.adapter.createProxy(Ice.stringToIdentity(pesel + str(acc_type)))

    if acc_type == Bank.AccountType.STANDARD:
      acc_prx = Bank.AccountPrx.uncheckedCast(base)
    else:
      acc_prx = Bank.PremiumAccountPrx.uncheckedCast(base)

    print('Registered ' + str(acc_type) + ' account for pesel ' + pesel)
    return Bank.RegistrationResponse(password=password, accountType=acc_type, account=acc_prx)

  def login(self, credentials, current=None):
    creds = Bank.UserCredentials(credentials).pesel
    self.user_manager.verify_credentials(creds.pesel, creds.password)
    acc_type = self.accounts[creds.pesel]._get_type()
    base = current.adapter.createProxy(Ice.stringToIdentity(creds.pesel + str(acc_type))) 
    if acc_type == Bank.AccountType.STANDARD:
      return Bank.AccountPrx.uncheckedCast(base)
    else:
      return Bank.PremiumAccountPrx.uncheckedCast(base)

  def _run(self):
    with Ice.initialize(sys.argv) as communicator:
      adapter = communicator.createObjectAdapterWithEndpoints("BankAdapter", "default -p " + self.service_port)
      adapter.add(self, communicator.stringToIdentity("BankAdapter"))
      adapter.activate()
      self.adapter = adapter
      self.communicator = communicator
      communicator.waitForShutdown()

  def start(self):
    bm = Thread(target=self._run)
    bm.start()
    self.currency_tracker.subscribe()
    bm.join()
