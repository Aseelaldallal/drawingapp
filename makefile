NAME = "Main"

all:
	@echo "Compiling..."
	javac *.java

run: all
	@echo "Running..."
	java $(NAME)

clean:
	rm -rf *.class
