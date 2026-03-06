CC = gcc
CFLAGS = -Wall -Wextra -g
INCLUDES = -IModels -IClient -IServer

# Directories
MODELS_DIR = Models
CLIENT_DIR = Client
SERVER_DIR = Server
BIN_DIR = bin
OBJ_DIR = obj

# Source files
MODELS_SRC = $(wildcard $(MODELS_DIR)/*.c)
CLIENT_SRC = $(wildcard $(CLIENT_DIR)/*.c)
SERVER_SRC = $(wildcard $(SERVER_DIR)/*.c)

# Object files
MODELS_OBJ = $(MODELS_SRC:$(MODELS_DIR)/%.c=$(OBJ_DIR)/%.o)
CLIENT_OBJ = $(CLIENT_SRC:$(CLIENT_DIR)/%.c=$(OBJ_DIR)/%.o)
SERVER_OBJ = $(SERVER_SRC:$(SERVER_DIR)/%.c=$(OBJ_DIR)/%.o)

# Target executables
CLIENT_TARGET = $(BIN_DIR)/clientTest
SERVER_TARGET = $(BIN_DIR)/serverTest

# Default target builds both client and server
all: directories $(CLIENT_TARGET) $(SERVER_TARGET)

# Create necessary directories
directories:
	@mkdir -p $(BIN_DIR)
	@mkdir -p $(OBJ_DIR)

# Build only the client
client: directories $(CLIENT_TARGET)

# Build only the server
server: directories $(SERVER_TARGET)

# Link the client executable
$(CLIENT_TARGET): $(MODELS_OBJ) $(CLIENT_OBJ)
	$(CC) $(CFLAGS) -o $@ $^

# Link the server executable
$(SERVER_TARGET): $(MODELS_OBJ) $(SERVER_OBJ)
	$(CC) $(CFLAGS) -o $@ $^

# Compile Models source files
$(OBJ_DIR)/%.o: $(MODELS_DIR)/%.c
	$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $@

# Compile Client source files
$(OBJ_DIR)/%.o: $(CLIENT_DIR)/%.c
	$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $@

# Compile Server source files
$(OBJ_DIR)/%.o: $(SERVER_DIR)/%.c
	$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $@

# Clean only client-related files
clean-client:
	rm -f $(CLIENT_OBJ) $(CLIENT_TARGET)

# Clean only server-related files
clean-server:
	rm -f $(SERVER_OBJ) $(SERVER_TARGET)

# Clean models files
clean-models:
	rm -f $(MODELS_OBJ)

# Clean everything
clean: clean-client clean-server clean-models
	rm -rf $(BIN_DIR) $(OBJ_DIR)

# Clean and rebuild everything
rebuild: clean all

# Clean and rebuild only client
rebuild-client: clean-client clean-models client

# Clean and rebuild only server
rebuild-server: clean-server clean-models server