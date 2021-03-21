#include "iostream"

#include "G4GDMLParser.hh"

using namespace std;

void parseGdml(const string &filename = "babyIaxo.gdml") {
    G4GDMLParser parser;
    cout << "Reading from '" << filename << "'" << endl;
    parser.Read(filename.c_str());
}

int main() {
    cout << "Starting..." << endl;
    parseGdml();
}
