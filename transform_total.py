'''

'''
import os
import json
import sys
import traceback
import cv2
import numpy as np

fix_w = 5000
fix_h = 5000


def API(file_path, output_path, filename, error_count):
    result = {}
    result["events"] = []
    img_ = np.full((fix_h, fix_w, 3), (255, 255, 255), np.uint8)
    with open(file_path, 'r') as f:
        try:
            data = json.load(f)
            for i, x in enumerate(data):
                # print("2: %d" % len(x['strokes']))
                for j, y in enumerate(x['strokes']):
                    # print(y)
                    # print("3: %d" % len(y))
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
        except:
            traceback.print_exc()
            error_count += 1

    out = open(output_path + "/" + filename + ".json", "w", encoding="utf-8")
    json.dump(result, out)

    # 顺便把删除相同点后的图片渲染一下
    for i, x in enumerate(result["events"]):
        for j, y in enumerate(x['x']):
            if j == 0:
                continue
            cv2.line(img_, (x['x'][j - 1], x['y'][j - 1]), (x['x'][j], x['y'][j]), (0, 0, 0), 2)
    cv2.imwrite(output_path + "/" + filename + ".jpg", img_)

    return error_count


import os

if __name__ == '__main__':
    work_dir = './cloudpen/'
    output_path = "./output"
    total = 0
    error_count = 0
    if not os.path.exists(output_path):
        os.makedirs(output_path)
    for parent, dirnames, filenames in os.walk(work_dir, followlinks=True):
        for i, filename in enumerate(filenames):
            file_path = os.path.join(parent, filename)
            if filename[-3:] == "pix":
                error_count = API(file_path, output_path, filename, error_count)
                total += 1
                print("%s done" % str(total))
            # print('文件名：%s' % filename)
            # print('文件完整路径：%s\n' % file_path)

o = open(output_path + "/error_rate.txt", "w")
temp = str(float(error_count) / total)
print("错误率：%s" % temp)
o.write(temp)
o.write("\n")
o.close()
