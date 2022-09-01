import sys
from expected import results
from importlib import import_module
def test_buggy_impl(impl_name, fname, args, idx):
   mod_name = impl_name[:-3]
   mod = import_module(mod_name)
   func = getattr(mod, fname)
   actual = func(*args)
   expected = results[idx]
   return (actual == expected)
if __name__ == "__main__":
   case_num = int(sys.argv[1])
   impl_name = sys.argv[2]
   fname = sys.argv[3]
   args = sys.argv[4:]
   new_args = [eval(arg) for arg in args]
   print(test_buggy_impl(impl_name, fname, new_args, case_num))
