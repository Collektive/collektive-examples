FROM python:3.13.7
RUN mkdir /experiment
WORKDIR /experiment
COPY .python-version ./
COPY process.py ./
COPY requirements.txt ./
RUN pip install --upgrade pip
RUN pip install -r requirements.txt
CMD python plotter_all.py
