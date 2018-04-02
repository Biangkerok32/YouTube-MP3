from __future__ import unicode_literals
from flask import Flask,request
import youtube_dl
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
        with youtube_dl.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])
        print(url)
    return "Hello World!"

if __name__ == '__main__':
    app.run(host='0.0.0.0',port=8080,debug=True)
