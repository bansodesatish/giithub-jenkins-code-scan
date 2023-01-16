import numpy as np
import pandas as pd
def lambda_handler(event, context):
    df2 = pd.DataFrame(np.array([[1, 2, 3], [4, 5, 6], [7, 8, 9]]),columns=["a", "b", "c"])
    number = np.pi
    print(df2)
    print(number)