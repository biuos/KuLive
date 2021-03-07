
all: demo-test

demo-test.o: demo-test.cc
	g++ -o demo-test.o -c demo-test.cc

srs_librtmp.o: srs_librtmp.cpp
	g++ -o srs_librtmp.o -c srs_librtmp.cpp

demo-test: demo-test.o srs_librtmp.o
	g++ -o demo-test demo-test.o srs_librtmp.o


.PHONE: clean
clean:
	rm -rf *.o demo-test
