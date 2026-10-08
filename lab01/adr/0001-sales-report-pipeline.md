# ADR 0001: Sales Report Pipeline Architecture

## Context
We need to process daily CSV sales files from multiple branches (PNH, REP, BTB) for a given month and output:
1. Standard console report summarizing total sales, transactions, top categories, and branch breakdowns.
2. Error handling log for bad rows/corrupted entries.

## Decision
- Use pure Java Standard Library without external third-party dependencies.
- Process CSV files line-by-line using Java Streams and Files API to keep memory footprint low.
- Implement robust exception handling for missing or malformed fields, skipping corrupted lines while logging errors.

## Consequences
- No build dependency configuration needed.
- Efficient streaming execution.
- Maintainable and modular structure.
