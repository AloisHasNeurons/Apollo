.PHONY: test clean build help

help:
	@echo "Usage:"
	@echo "  make test    Run local unit tests"
	@echo "  make clean   Clean build artifacts"
	@echo "  make build   Build the debug APK"

test:
	./gradlew testDebugUnitTest

clean:
	./gradlew clean

build:
	./gradlew assembleDebug
