def func0(intval):
    return intval

import sys
if __name__ == "__main__":
  args = sys.argv[1:]
  args = [eval(arg) for arg in args]
  result = func0(*args)
  print(result)
