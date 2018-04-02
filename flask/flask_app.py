#!/usr/bin/python
from __future__ import unicode_literals
from flask import Flask,request
import youtube_dl
import os
app = Flask(__name__)

@app.route("/hello",methods=['GET','POST'])
def hello():
    if request.method == 'POST':
        t = str(request.get_data())
        t = t.split('=')
        id = t[1].strip("'")
        print(id)
        url = "https://www.youtube.com/watch?v="+t[1]
        ydl_opts = {
                'format':'bestaudio/best',
                'outtmpl':"mp3-"+id+".mp3",
                'postprocessors': [{
                    'key':'FFmpegExtractAudio',
                    'preferredcodec':'mp3',
                    'preferredquality':'160',
                    }],
                }
        info_dict = None
        with youtube_dl.YoutubeDL(ydl_opts) as ydl:
            info_dict = ydl.extract_info(url,download=False)
            ydl.download([url])
        print(url)
    return info_dict.get('title',None)

if __name__ == '__main__':
    app.run(host='0.0.0.0',port=5000,debug=True)
