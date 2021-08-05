'''

'''
import os
import json
import sys

result = {}
result["events"] = []
count = 1
with open('input/4_2fb3ef44b391af3d1e9e4c6ae45cc02e_pix', 'r') as f:
    data = json.load(f)
    print("1: %d" % len(data))
    for i, x in enumerate(data):
        print("2: %d" % len(x['strokes']))
        for j, y in enumerate(x['strokes']):
            print(y)
            print("3: %d" % len(y))
            temp_dict = {}
            temp_dict["pointerType"] = "PEN"
            temp_dict["pointerId"] = ("1")
            x_list = []
            y_list = []
            p_list = []
            t_list = []
            for k, z in enumerate(y):
                x_list.append(z[0])
                y_list.append(z[1])
                p_list.append(z[2])
                t_list.append(z[3])
            temp_dict["x"] = x_list
            temp_dict["y"] = y_list
            temp_dict["p"] = p_list
            temp_dict["t"] = t_list
            result["events"].append(temp_dict)
            count += 1
        # print(x)

print("%d 个主体部分" % len(result["events"]))
save_str = json.dumps(result)
out = open("./pointerEvents.json", "w", encoding="utf-8")
json.dump(result, out)
